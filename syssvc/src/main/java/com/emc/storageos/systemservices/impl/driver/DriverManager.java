package com.emc.storageos.systemservices.impl.driver;

import static com.emc.storageos.coordinator.client.model.Constants.CONTROL_NODE_SYSSVC_ID_PATTERN;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.storageos.coordinator.client.model.Site;
import com.emc.storageos.coordinator.client.model.StorageDriverMetaData;
import com.emc.storageos.coordinator.client.model.StorageDriversInfo;
import com.emc.storageos.coordinator.client.service.CoordinatorClient;
import com.emc.storageos.coordinator.client.service.DrUtil;
import com.emc.storageos.coordinator.client.service.NodeListener;
import com.emc.storageos.coordinator.common.Service;
import com.emc.storageos.db.client.DbClient;
import com.emc.storageos.db.client.URIUtil;
import com.emc.storageos.db.client.model.StorageSystemType;
import com.emc.storageos.services.util.NamedThreadPoolExecutor;
import com.emc.storageos.services.util.Waiter;
import com.emc.storageos.svcs.errorhandling.resources.APIException;
import com.emc.storageos.systemservices.impl.client.SysClientFactory;
import com.emc.storageos.systemservices.impl.property.PropertyManager;
import com.emc.storageos.systemservices.impl.upgrade.CoordinatorClientExt;
import com.emc.storageos.systemservices.impl.upgrade.LocalRepository;
import com.google.common.io.Files;

public class DriverManager {

    public static final String DRIVER_DIR = "/data/drivers/";
    public static final String TMP_DIR = "/tmp/";
    public static final String CONTROLLER_SERVICE = "controllersvc";

    private static final String LISTEN_PATH = String.format("/config/%s/%s", StorageDriversInfo.KIND,
            StorageDriversInfo.ID);
    private static final Logger log = LoggerFactory.getLogger(PropertyManager.class);
    private static final String DRIVERS_UPDATE_LOCK = "driversupdatelock";
    private static final ThreadPoolExecutor EXECUTOR = new NamedThreadPoolExecutor("DriverUpdateThead", 1);

    private Set<String> localDrivers;
    private Set<String> targetDrivers;
    private CoordinatorClientExt coordinator;
    private CoordinatorClient coordinatorClient;
    private DrUtil drUtil;
    private DbClient dbClient;
    private Service service;
    private Waiter waiter = new Waiter();
//    private URI initNode; // Node that has been synced with latest driver list
    private LocalRepository localRepo = LocalRepository.getInstance();

    private Set<String> toRemove;
    private Set<String> toDownload;

    public void setCoordinator(CoordinatorClientExt coordinator) {
        this.coordinator = coordinator;
        this.coordinatorClient = coordinator.getCoordinatorClient();
        this.drUtil = new DrUtil(coordinatorClient);
    }

    public DbClient getDbClient() {
        return dbClient;
    }

    public void setDbClient(DbClient dbClient) {
        this.dbClient = dbClient;
    }

    public void setService(Service service) {
        this.service = service;
    }

    private void restartControllerServices() {
        for (String node : coordinator.getAllNodeIds()) {
            localRepo.removeRestartService(node, CONTROLLER_SERVICE);
        }
    }

    private boolean areAllNodesUpdated() throws Exception {
        for (Site site : drUtil.listSites()) {
            Map<Service, StorageDriversInfo> localInfos = coordinator.getAllNodeInfos(StorageDriversInfo.class,
                    CONTROL_NODE_SYSSVC_ID_PATTERN, site.getUuid());
            for (Map.Entry<Service, StorageDriversInfo> info : localInfos.entrySet()) {
                if (!targetDrivers.equals(info.getValue().getInstalledDrivers())) {
                    log.info("Drivers on node {} have not been updated", info.getKey().getName());
                    log.info("Target drivers: {}", Arrays.toString(targetDrivers.toArray()));
                    log.info("Local drivers on node {}: {}", info.getKey().getName(),
                            Arrays.toString(info.getValue().getInstalledDrivers().toArray()));
                    return false;
                }
            }
        }
        log.info("All nodes's drivers are synced with target list");
        return true;
    }

    private List<StorageSystemType> queryDriversByStatus(StorageSystemType.STATUS status) {
        List<StorageSystemType> types = new ArrayList<StorageSystemType>();
        List<URI> ids = dbClient.queryByType(StorageSystemType.class, true);
        Iterator<StorageSystemType> it = dbClient.queryIterativeObjects(StorageSystemType.class, ids);
        while (it.hasNext()) {
            StorageSystemType type = it.next();
            if (StringUtils.equals(type.getStatus(), status.toString())) {
                types.add(type);
            }
        }
        return types;
    }

    private void updateMetaData() {
        Site activeSite = drUtil.getActiveSite();
        List<StorageDriversInfo>  infos = getDriversInfo(activeSite.getUuid());
        if (activeSite.getNodeCount() != infos.size()) {
            log.error("There's node down in active site");
            return;
        }
        for (Site site : drUtil.listStandbySites()) {
            List<StorageDriversInfo> standbyInfos = getDriversInfo(site.getUuid());
            if (site.getNodeCount() != standbyInfos.size()) {
                log.error("There's node down in standby site {}", site.getName());
                return;
            }
            infos.addAll(standbyInfos);
        }
        
        boolean needRestart = false;
        // If driver to install has been on every node, set to active
        List<StorageSystemType> installingTypes = queryDriversByStatus(StorageSystemType.STATUS.INSTALLING);
        for (StorageSystemType type : installingTypes) {
            boolean finished = true;
            for (StorageDriversInfo info :infos) {
                if (!info.getInstalledDrivers().contains(type.getDriverFileName())) {
                    finished = false;
                    break;
                }
            }
            if (finished) {
                type.setStatus(StorageSystemType.STATUS.ACTIVE.toString());
                dbClient.updateObject(type);
                log.info("update status from installing to active for {}", type.getStorageTypeName());
                needRestart = true;
            }
        }
        // if driver to uninstall has been deleted on every node, dete it in db
        List<StorageSystemType> uninstallingTypes =  queryDriversByStatus(StorageSystemType.STATUS.UNISNTALLING);
        for (StorageSystemType type : uninstallingTypes) {
            boolean finished = true;
            for (StorageDriversInfo info :infos) {
                if (info.getInstalledDrivers().contains(type.getDriverFileName())) {
                    finished = false;
                    break;
                }
            }
            if (finished) {
                dbClient.removeObject(type);
                log.info("Remove {}", type.getStorageTypeName());
                needRestart = true;
            }
        }
        // handle upgrading ones
        List<StorageSystemType> upgradingTypes =  queryDriversByStatus(StorageSystemType.STATUS.UPGRADING);
        for (StorageSystemType type : upgradingTypes) {
            // TODO if found new meta data in ZK, insert it to db and delete it from zk
            // TODO if not found new meta data in ZK, set status to ACTIVE
            // TODO remember to set needRestart flag, to restart controller services
        }
        if  (needRestart) {
            restartControllerServices();
        }
    }

    private List<StorageDriversInfo> getDriversInfo(String siteId) {
        List<StorageDriversInfo> infos = new ArrayList<StorageDriversInfo>();
        try {
            Map<Service, StorageDriversInfo> localInfos = coordinator.getAllNodeInfos(StorageDriversInfo.class,
                    CONTROL_NODE_SYSSVC_ID_PATTERN, siteId);
            for (Map.Entry<Service, StorageDriversInfo> info : localInfos.entrySet()) {
                infos.add(info.getValue());
            }
        } catch (Exception e) {
            log.error("Error happened when geting drivers info for site {}", siteId);
        }
        return infos;
    }

    private boolean hasActiveSiteFinishDownload(String driverFileName) {
        Site activeSite = drUtil.getActiveSite();
        String activeSiteId = activeSite.getUuid();
        List<StorageDriversInfo> infos = getDriversInfo(activeSiteId);
        if (activeSite.getNodeCount() != infos.size()) {
            // there're offline node in active site
            return false;
        }
        for (StorageDriversInfo info : infos) {
            if (!info.getInstalledDrivers().contains(driverFileName)) {
                return false;
            }
        }
        return true;
    }

    private void removeDrivers(Set<String> drivers) {
        for (String driver : drivers) {
            log.info("removing driver file: {}", driver);
            LocalRepository.getInstance().removeStorageDriver(driver);
        }
    }

    private void downloadDrivers(Set<String> drivers) {
        for (String driver : drivers) {
            File driverFile = new File(TMP_DIR + driver);
            try {
                URI endPoint = getSyncedNode(driver);
                if (endPoint == null) {
                    // should not happen
                    log.error("Can't find node that hold driver file: {}", driver);
                    continue;
                }
                if (drUtil.isStandby()) { // need to wait active site all finished, and substitute endPoint to active vip
                    while (!hasActiveSiteFinishDownload(driver)) {
                        log.info("Sleep 5 seconds to wait active site finish downloading driver {}", driver);
                        waiter.sleep(5000); // sleep 5 seconds to retry
                    }
                    // modify endpoint
                    Site activeSite = drUtil.getActiveSite();
                    endPoint = URI.create(String.format(SysClientFactory.BASE_URL_FORMAT,
                            activeSite.getVipEndPoint(), service.getEndpoint().getPort()));
                }
                String uri = SysClientFactory.URI_GET_DRIVER + "?name=" + driver;
                InputStream in = SysClientFactory.getSysClient(endPoint).get(new URI(uri),
                        InputStream.class, MediaType.APPLICATION_OCTET_STREAM);

                OutputStream os = new BufferedOutputStream(new FileOutputStream(driverFile));
                int bytesRead = 0;
                while (true) {
                    byte[] buffer = new byte[0x10000];
                    bytesRead = in.read(buffer);
                    if (bytesRead == -1) {
                        break;
                    }
                    os.write(buffer, 0, bytesRead);
                }
                in.close();
                os.close();
                Files.move(driverFile, new File(DRIVER_DIR + driverFile.getName()));
                log.info("Driver {} has been downloaded from {}", driver, endPoint);
            } catch (Exception e) {
                log.error("Failed to download driver {} with exception", driver, e);
            }
        }
    }

    /**
     * @return elements who are included in original list but not in subtractor list
     */
    private Set<String> minus(Set<String> original, Set<String> subtractor) {
        Set<String> result = new HashSet<String>();
        for (String element : original) {
            if (subtractor.contains(element)) {
                continue;
            }
            result.add(element);
        }
        return result;
    }

    private void initializeLocalAndTargetInfo() {
        localDrivers = localRepo.getLocalDrivers();
        log.info("Local drivers initialized: {}", Arrays.toString(localDrivers.toArray()));

        StorageDriversInfo targetInfo = coordinator.getTargetInfo(StorageDriversInfo.class);
        if (targetInfo == null) {
            targetInfo = new StorageDriversInfo();
            targetInfo.setInstalledDrivers(localDrivers);
            coordinator.setTargetInfo(targetInfo);
            log.info("Can't find target storage drivers info, so init it with local drivers list");
        }
        targetDrivers = targetInfo.getInstalledDrivers();
        log.info("Target drivers info initialized: {}", Arrays.toString(targetDrivers.toArray()));
    }

    private void registerLocalDrivers() {
        StorageDriversInfo info = new StorageDriversInfo();
        info.setInstalledDrivers(localRepo.getLocalDrivers());
        coordinator.setNodeSessionScopeInfo(info);
    }

    private void addDriverInfoListener() {
        try {
            coordinator.getCoordinatorClient().addNodeListener(new DriverInfoListener());
        } catch (Exception e) {
            log.error("Fail to add node listener for driver info target znode", e);
            throw APIException.internalServerErrors.addListenerFailed();
        }
        log.info("Successfully added node listener for driver info target znode");
    }

    /**
     * Update locally installed drivers list to syssvc service beacon
     */
    public void updateLocalDriversList() {
        localDrivers = localRepo.getLocalDrivers();
        StorageDriversInfo info = new StorageDriversInfo();
        info.setInstalledDrivers(localDrivers);
        coordinator.setNodeSessionScopeInfo(info);
    }

    /**
     * 
     * @param driverFileName
     * @return node who holds driver file from active site
     */
    private URI getSyncedNode(String driverFileName) {
        try {
            String activeSiteId = drUtil.getActiveSite().getUuid();
            Map<Service, StorageDriversInfo> localInfos = coordinator.getAllNodeInfos(StorageDriversInfo.class,
                    CONTROL_NODE_SYSSVC_ID_PATTERN, activeSiteId);
            List<String> candidates = new ArrayList<>();
            for (Map.Entry<Service, StorageDriversInfo> info : localInfos.entrySet()) {
                if (info.getValue().getInstalledDrivers().contains(driverFileName)) {
                    candidates.add(info.getKey().getId());
                    log.info("Add node {} to synced nodes list", info.getKey().getId());
                }
            }

            if (!candidates.isEmpty()) {
                String syssvcId = candidates.get(new Random().nextInt(candidates.size()));
                return coordinator.getNodeEndpointForSvcId(syssvcId);
            } else {
                // This should not happen
                log.error("There's no synced node for {} now", driverFileName);
            }
        } catch (Exception e) {
            log.error("Can't find node in sync with target drivers list");
        }
        return null;
    }

    /**
     * Check and update local drivers asynchronously, so not to block
     * notification thread
     */
    private void checkAndUpdate() {
        EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                initializeLocalAndTargetInfo();

                // remove drivers and restart controller service
                toRemove = minus(localDrivers, targetDrivers);
                toDownload = minus(targetDrivers, localDrivers);
                if (toRemove.isEmpty() && toDownload.isEmpty()) {
                    log.info("Local installed drivers list is synced with target, return");
                    return;
                }

                if (!toRemove.isEmpty()) {
                    removeDrivers(toRemove);
                }

                if (!toDownload.isEmpty()) {
                    downloadDrivers(toDownload);
                }

                // update progress to ZK
                updateLocalDriversList();

                // Active site need to update medata and restart all controller services
                if (drUtil.isActiveSite()) {
                    InterProcessLock lock = null;
                    try {
                        lock = getLock(DRIVERS_UPDATE_LOCK);
                        if (areAllNodesUpdated()) {
                            updateMetaData(); // only for install and uninstall
                        }
                    } catch (Exception e) {
                        log.error("error happend when updating driver info", e);
                    } finally {
                        if (lock != null) {
                            try {
                                lock.release();
                            } catch (Exception ignore) {
                                log.warn("lock release failed");
                            }
                        }
                    }
                }
            }
        });
    }

    private InterProcessLock getLock(String name) throws Exception {
        InterProcessLock lock = null;
        while (true) {
            try {
                lock = coordinator.getCoordinatorClient().getLock(name); // global
                                                                         // lock
                                                                         // across
                                                                         // all
                                                                         // sites
                lock.acquire();
                break; // got lock
            } catch (Exception e) {
                if (coordinator.isConnected()) {
                    throw e;
                }
            }
        }
        return lock;
    }

    public void start() {
        addDriverInfoListener();
        registerLocalDrivers();
        checkAndUpdate();
    }
    class DriverInfoListener implements NodeListener {
        @Override
        public String getPath() {
            return LISTEN_PATH;
        }

        @Override
        public void connectionStateChanged(State state) {
            log.info("Driver info connection state changed to {}", state);
            if (state != State.CONNECTED) {
                return;
            }
            log.info("Curator (re)connected. Try to pull latest info and update local driver if necessary ...");
            checkAndUpdate();
        }

        @Override
        public void nodeChanged() throws Exception {
            log.info("Driver info changed. Try to pull latest info and update local driver if necessary ...");
            checkAndUpdate();
        }
    }

    public static List<StorageSystemType> convert (StorageDriverMetaData driver) {
        List<StorageSystemType> types = new ArrayList<StorageSystemType>();
        StorageSystemType type = new StorageSystemType();
        type.setStorageTypeName(driver.getStorageName());
        type.setStorageTypeDispName(driver.getStorageDisplayName());
        type.setDriverName(driver.getDriverName());
        type.setDriverVersion(driver.getDriverVersion());
        type.setDriverFileName(driver.getDriverFileName());
        type.setMetaType(driver.getMetaType());
        URI uri = URIUtil.createId(StorageSystemType.class);
        type.setId(uri);
        type.setStorageTypeId(uri.toString());
        type.setIsDefaultSsl(driver.isEnableSsl());
        type.setSslPort(Long.toString(driver.getSslPort()));
        type.setNonSslPort(Long.toString(driver.getNonSslPort()));
        type.setDriverClassName(driver.getDriverClassName());
        types.add(type);

        if (StringUtils.isNotEmpty(driver.getProviderName())
                && StringUtils.isNotEmpty(driver.getProviderDisplayName())) {
            StorageSystemType provider = new StorageSystemType();
            provider.setStorageTypeName(driver.getProviderName());
            provider.setStorageTypeDispName(driver.getProviderDisplayName());
            provider.setIsSmiProvider(true);
            provider.setDriverName(driver.getDriverName());
            provider.setDriverVersion(driver.getDriverVersion());
            provider.setDriverFileName(driver.getDriverFileName());
            provider.setMetaType(driver.getMetaType());
            uri = URIUtil.createId(StorageSystemType.class);
            provider.setId(uri);
            provider.setStorageTypeId(uri.toString());
            provider.setIsDefaultSsl(driver.isEnableSsl());
            provider.setSslPort(Long.toString(driver.getSslPort()));
            provider.setNonSslPort(Long.toString(driver.getNonSslPort()));
            provider.setDriverClassName(driver.getDriverClassName());
            types.add(provider);
        }
        return types;
    }
}
