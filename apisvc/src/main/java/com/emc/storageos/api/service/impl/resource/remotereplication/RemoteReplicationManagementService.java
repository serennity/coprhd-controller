/*
 * Copyright (c) 2017 EMC Corporation
 * All Rights Reserved
 */
package com.emc.storageos.api.service.impl.resource.remotereplication;


import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.emc.storageos.api.service.impl.resource.ArgValidator;
import com.emc.storageos.db.client.model.DataObject;
import com.emc.storageos.db.client.model.DiscoveredDataObject;
import com.emc.storageos.db.client.model.FileShare;
import com.emc.storageos.db.client.model.Volume;
import com.emc.storageos.storagedriver.model.remotereplication.RemoteReplicationOperationContext;
import com.emc.storageos.svcs.errorhandling.resources.APIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.storageos.api.service.impl.resource.TaskResourceService;
import com.emc.storageos.db.client.model.remotereplication.RemoteReplicationGroup;
import com.emc.storageos.db.client.model.remotereplication.RemoteReplicationPair;
import com.emc.storageos.db.client.model.remotereplication.RemoteReplicationSet;
import com.emc.storageos.db.client.model.remotereplication.RemoteReplicationPair.ElementType;
import com.emc.storageos.model.ResourceTypeEnum;
import com.emc.storageos.model.TaskList;
import com.emc.storageos.model.TaskResourceRep;
import com.emc.storageos.model.remotereplication.RemoteReplicationOperationParam;
import com.emc.storageos.model.remotereplication.RemoteReplicationOperationParam.OperationContext;
import com.emc.storageos.remotereplicationcontroller.RemoteReplicationUtils;
import com.emc.storageos.security.authorization.ACL;
import com.emc.storageos.security.authorization.DefaultPermissions;
import com.emc.storageos.security.authorization.Role;
import com.emc.storageos.svcs.errorhandling.resources.InternalException;

@Path("/vdc/block/remotereplicationmanagement")
@DefaultPermissions(readRoles = { Role.SYSTEM_ADMIN, Role.SYSTEM_MONITOR, Role.TENANT_ADMIN }, readAcls = {
        ACL.OWN, ACL.ALL }, writeRoles = { Role.SYSTEM_ADMIN, Role.TENANT_ADMIN }, writeAcls = { ACL.OWN,
        ACL.ALL })
public class RemoteReplicationManagementService extends TaskResourceService {

    private static final Logger _log = LoggerFactory.getLogger(RemoteReplicationManagementService.class);
    public static final String SERVICE_TYPE = "remote_replication_management";


    // remote replication service api implementations
    private RemoteReplicationBlockServiceApiImpl remoteReplicationServiceApi;

    private RemoteReplicationGroupService rrGroupService;
    private RemoteReplicationPairService rrPairService;
    private RemoteReplicationSetService rrSetService;

    public RemoteReplicationBlockServiceApiImpl getRemoteReplicationServiceApi() {
        return remoteReplicationServiceApi;
    }

    public void setRemoteReplicationServiceApi(RemoteReplicationBlockServiceApiImpl remoteReplicationServiceApi) {
        this.remoteReplicationServiceApi = remoteReplicationServiceApi;
    }

    public RemoteReplicationGroupService getRrGroupService() {
        return rrGroupService;
    }

    public void setRrGroupService(RemoteReplicationGroupService groupService) {
        this.rrGroupService = groupService;
    }

    public RemoteReplicationPairService getRrPairService() {
        return rrPairService;
    }

    public void setRrPairService(RemoteReplicationPairService rrPairService) {
        this.rrPairService = rrPairService;
    }

    public RemoteReplicationSetService getRrSetService() {
        return rrSetService;
    }

    public void setRrSetService(RemoteReplicationSetService rrSetService) {
        this.rrSetService = rrSetService;
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    protected ResourceTypeEnum getResourceType() {
        return ResourceTypeEnum.REMOTE_REPLICATION_MANAGEMENT;
    }

    @Override
    protected RemoteReplicationPair queryResource(URI id) {
       return null;
    }

    @Override
    protected URI getTenantOwner(URI id) {
        return null;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Path("/establish")
    public TaskList establishRemoteReplicationLink(RemoteReplicationOperationParam operationParam) throws InternalException {
        _log.info("Called: establishRemoteReplicationLink() with context {} and ids {}",
                operationParam.getOperationContext(), operationParam.getIds());

        validateContainmentForContext(operationParam);

        RemoteReplicationOperationParam.OperationContext operationContext =
                RemoteReplicationOperationParam.OperationContext.valueOf(operationParam.getOperationContext());

        TaskResourceRep task = null;
        TaskList taskList = new TaskList();
        RemoteReplicationPair rrPair;

        switch (operationContext) {
            case RR_PAIR:
                // for individual pairs send one request for each pair
                // call pair service for each pair and add task to the taskList, return task list.
                String taskID = UUID.randomUUID().toString();
                for (URI rrPairURI : operationParam.getIds()) {
                    TaskResourceRep rrPairTaskResourceRep = rrPairService.establishRemoteReplicationPairLink(rrPairURI, taskID);
                    taskList.addTask(rrPairTaskResourceRep);
                }
                break;
            case RR_GROUP_CG:
            case RR_SET_CG:
                taskList =  rrPairService.establishRemoteReplicationCGLink(operationParam.getIds());
                break;
            case RR_GROUP:
                rrPair = _dbClient.queryObject(RemoteReplicationPair.class, operationParam.getIds().get(0));
                URI groupURI = rrPair.getReplicationGroup();
                RemoteReplicationGroup rrGroup = _dbClient.queryObject(RemoteReplicationGroup.class, groupURI);
                task =  rrGroupService.establishRemoteReplicationGroupLink(rrGroup.getId());
                taskList.addTask(task);
                break;

            case RR_SET:
                rrPair = _dbClient.queryObject(RemoteReplicationPair.class, operationParam.getIds().get(0));
                URI setURI = rrPair.getReplicationSet();
                RemoteReplicationSet rrSet = _dbClient.queryObject(RemoteReplicationSet.class, setURI);
                task =  rrSetService.establishRemoteReplicationSetLink(rrSet.getId());
                taskList.addTask(task);
                break;
        }
        return taskList;
    }


    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Path("/failback")
    public TaskList failbackRemoteReplicationLink(RemoteReplicationOperationParam operationParam) throws InternalException {
        _log.info("Called: failbackRemoteReplicationLink() with context {} and ids {}",
                operationParam.getOperationContext(), operationParam.getIds());

        validateContainmentForContext(operationParam);

        RemoteReplicationOperationParam.OperationContext operationContext =
                RemoteReplicationOperationParam.OperationContext.valueOf(operationParam.getOperationContext());

        TaskResourceRep task = null;
        TaskList taskList = new TaskList();
        RemoteReplicationPair rrPair;

        switch (operationContext) {
            case RR_PAIR:
                // for individual pairs send one request for each pair
                // call pair service for each pair and add task to the taskList, return task list.
                String taskID = UUID.randomUUID().toString();
                for (URI rrPairURI : operationParam.getIds()) {
                    TaskResourceRep rrPairTaskResourceRep = rrPairService.failbackRemoteReplicationPairLink(rrPairURI, taskID);
                    taskList.addTask(rrPairTaskResourceRep);
                }
                break;
            case RR_GROUP_CG:
            case RR_SET_CG:
                taskList =  rrPairService.failbackRemoteReplicationCGLink(operationParam.getIds());
                break;
            case RR_GROUP:
                rrPair = _dbClient.queryObject(RemoteReplicationPair.class, operationParam.getIds().get(0));
                URI groupURI = rrPair.getReplicationGroup();
                RemoteReplicationGroup rrGroup = _dbClient.queryObject(RemoteReplicationGroup.class, groupURI);
                task =  rrGroupService.failbackRemoteReplicationGroupLink(rrGroup.getId());
                taskList.addTask(task);
                break;

            case RR_SET:
                rrPair = _dbClient.queryObject(RemoteReplicationPair.class, operationParam.getIds().get(0));
                URI setURI = rrPair.getReplicationSet();
                RemoteReplicationSet rrSet = _dbClient.queryObject(RemoteReplicationSet.class, setURI);
                task =  rrSetService.failbackRemoteReplicationSetLink(rrSet.getId());
                taskList.addTask(task);
                break;
        }
        return taskList;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Path("/failover")
    public TaskList failoverRemoteReplicationLink(RemoteReplicationOperationParam operationParam) throws InternalException {
        _log.info("Called: failoverRemoteReplicationLink() with context {} and ids {}",
                operationParam.getOperationContext(), operationParam.getIds());

        validateContainmentForContext(operationParam);

        RemoteReplicationOperationParam.OperationContext operationContext =
                RemoteReplicationOperationParam.OperationContext.valueOf(operationParam.getOperationContext());

        TaskResourceRep task = null;
        TaskList taskList = new TaskList();
        RemoteReplicationPair rrPair = _dbClient.queryObject(RemoteReplicationPair.class, operationParam.getIds().get(0));

        Volume volume = _dbClient.queryObject(Volume.class, rrPair.getSourceElement());
        if (volume.getSystemType().equalsIgnoreCase(DiscoveredDataObject.Type.vmax.toString()) ||
            volume.getSystemType().equalsIgnoreCase(DiscoveredDataObject.Type.vmax3.toString())) {
            // delegate to SRDF support
            return processSrdfLinkRequest(operationContext, operationParam.getIds());
        }

        switch (operationContext) {
            case RR_PAIR:
                // for individual pairs send one request for each pair
                // call pair service for each pair and add task to the taskList, return task list.
                String taskID = UUID.randomUUID().toString();
                for (URI rrPairURI : operationParam.getIds()) {
                    TaskResourceRep rrPairTaskResourceRep = rrPairService.failoverRemoteReplicationPairLink(rrPairURI, taskID);
                    taskList.addTask(rrPairTaskResourceRep);
                }
                break;
            case RR_GROUP_CG:
            case RR_SET_CG:
                taskList =  rrPairService.failoverRemoteReplicationCGLink(operationParam.getIds());
                break;
            case RR_GROUP:
                URI groupURI = rrPair.getReplicationGroup();
                RemoteReplicationGroup rrGroup = _dbClient.queryObject(RemoteReplicationGroup.class, groupURI);
                task =  rrGroupService.failoverRemoteReplicationGroupLink(rrGroup.getId());
                taskList.addTask(task);
                break;

            case RR_SET:
                URI setURI = rrPair.getReplicationSet();
                RemoteReplicationSet rrSet = _dbClient.queryObject(RemoteReplicationSet.class, setURI);
                task =  rrSetService.failoverRemoteReplicationSetLink(rrSet.getId());
                taskList.addTask(task);
                break;
        }
        return taskList;
    }


    private Set<URI> getSetPairIdsByRrSet(URI rrSetId) {
        List<RemoteReplicationPair> pairs = RemoteReplicationUtils.findAllRemoteRepliationPairsByRrSet(rrSetId, _dbClient);
        Set<URI> setPairIds = new HashSet<>();
        for (RemoteReplicationPair pair : pairs) {
            if (!pair.isGroupPair()) {
                setPairIds.add(pair.getId());
            }
        }
        return setPairIds;
    }

    private Set<URI> getGroupPairIdsByRrGroup(URI rrGroupId) {
        List<RemoteReplicationPair> pairs = RemoteReplicationUtils.findAllRemoteRepliationPairsByRrGroup(rrGroupId, _dbClient);
        Set<URI> setPairIds = new HashSet<>();
        for (RemoteReplicationPair pair : pairs) {
            if (pair.isGroupPair()) {
                setPairIds.add(pair.getId());
            }
        }
        return setPairIds;
    }

    /**
     * Validate parameters according to context of the operation.
     * @param operationParam
     */
    void validateContainmentForContext(RemoteReplicationOperationParam operationParam) {
        /*
         * Validate that remote replication pairs and context are valid with regard to containment.
         * Validate that parameters have valid remote replication pair ids.
         * Validate that ids match context from containment point of view:
         *   --- if context is RR_SET, all ids should be in the same remote replication set and contain all set pairs
         *        (group pairs and set pairs)
         *   --- if context is RR_GROUP, all ids should be in the same group and contain all group pairs
         *   --- if context is RR_PAIR, we do not enforce any additional validation
         *   --- if context is RR_SET_CG, all ids should be direct set pairs and their source volumes should be in the
         *       same CG; all target volumes should be in the same target CG
         *   --- if context is RR_GROUP_CG, all ids should be part of remote replication group and their source volumes should be in the
         *       same CG; all target volumes should be in the same target CG
         *
         *
         */
        OperationContext context = validateOperationContext(operationParam.getOperationContext());
        Set<URI> rrPairIds = new HashSet<>(operationParam.getIds());
        for (URI rrPairId : rrPairIds) {
            ArgValidator.checkFieldUriType(rrPairId, RemoteReplicationPair.class, "id");
        }

        List<RemoteReplicationPair> rrPairs =_dbClient.queryObject(RemoteReplicationPair.class, rrPairIds);
        if (rrPairs.isEmpty()) {
            throw APIException.badRequests.remoteReplicationOperationPrecheckFailed("remote repliation pair IDs are not found");
        }
        

        switch (context) {
            case RR_SET:
                checkContainment(rrPairs, false, true, false);
                URI rrSetId = rrPairs.get(0).getReplicationSet();
                Set<URI> setPairIds = getSetPairIdsByRrSet(rrSetId);
                if (!rrPairIds.containsAll(setPairIds)) {
                    throw APIException.badRequests.remoteReplicationOperationPrecheckFailed(String.format(
                            "Given remote repliation pair ids should contain all set pairs of remote repliation set %s",
                            rrSetId));
                }
                break;
            case RR_GROUP:
                checkContainment(rrPairs, true, true, false);
                URI rrGroupId = rrPairs.get(0).getReplicationGroup();
                Set<URI> groupPairIds =getGroupPairIdsByRrGroup(rrGroupId);
                if (!rrPairIds.containsAll(groupPairIds)) {
                    throw APIException.badRequests.remoteReplicationOperationPrecheckFailed(String.format(
                            "Given remote repliation pair ids should contain all set pairs of remote repliation group %s",
                            rrGroupId));
                }
                break;
            case RR_PAIR:
                // No additional validation needed
                break;
            case RR_SET_CG:
                checkContainment(rrPairs, false, false, true);
                break;
            case RR_GROUP_CG:
                checkContainment(rrPairs, true, true, true);
                break;
        }
    }

    private OperationContext validateOperationContext(String contextStr) {
        try {
            return OperationContext.valueOf(contextStr);
        } catch (Exception e) {
            throw APIException.badRequests.invalidRemoteReplicationContext(contextStr);
        }
    }

    /**
     * Validate the remote replication pairs based on given parameters described as following:
     * @param rrPairs
     *            the remote replication pair IDs
     * @param inSameGroup
     *            check whether all rr pairs have the same rr group if this param is true,
     *            otherwise check whether all rr pairs have the same rr set
     * @param allowInGroup
     *            when checking whether all rr pairs are in the same rr set,
     *            throw exception if one of the rr pairs is in a rr group and this param is false
     *            NOTE: this parameter is only used when inSameGroup is false
     * @param checkCG
     *            Check if all the source/target volumes are in the same consistency groups.
     */
    private void checkContainment(List<RemoteReplicationPair> rrPairs, boolean inSameGroup, boolean allowInGroup,
            boolean checkCG) {
        URI uri = null;
        URI sourceCG = null;
        URI targetCG = null;
        for (RemoteReplicationPair pair : rrPairs) {
            // When operation context is RR_SET_CG, pairs should be directly contained by rr set
            if (!inSameGroup && !allowInGroup && pair.isGroupPair()) {
                throw APIException.badRequests.remoteReplicationOperationPrecheckFailed(String.format(
                        "remote repliation pair %s is not directly contained in remote repliation group, which is not allowed",
                        pair.getNativeId()));
            }
            // Check all pairs have the same rr pair or rr group
            URI currentPairOrGroup = inSameGroup ? pair.getReplicationGroup() : pair.getReplicationSet();
            if (currentPairOrGroup == null) {
                throw APIException.badRequests.remoteReplicationOperationPrecheckFailed(
                        String.format("remote replication %s of remote repliaction pair %s is null, which is not allowed",
                                inSameGroup ? "group" : "set", pair.getNativeId()));
            } else if (uri == null) {
                uri = currentPairOrGroup;
            } else if (!uri.equals(currentPairOrGroup)) {
                throw APIException.badRequests.remoteReplicationOperationPrecheckFailed(String.format(
                        "remote replication %s of remote replication pair %s is not the same as the others",
                        inSameGroup ? "group" : "set", pair.getNativeId()));
            }
            if (!checkCG) {
                continue;
            }
            // Check all pairs have same source and target consistency groups
            URI currentSourceCG = _dbClient.queryObject(Volume.class, pair.getSourceElement()).getConsistencyGroup();
            if (currentSourceCG == null) {
                throw APIException.badRequests.remoteReplicationOperationPrecheckFailed(String.format(
                        "remote replication pair %'s source volume has no consistency group", pair.getNativeId()));
            } else if (sourceCG == null) {
                sourceCG = currentSourceCG;
            } else if (!sourceCG.equals(currentSourceCG)) {
                throw APIException.badRequests.remoteReplicationOperationPrecheckFailed(String.format(
                        "remote repliation pair %s's source target volume's consistency group is not the same as others",
                        pair.getNativeId()));
            }

            URI currentTargetCG = _dbClient.queryObject(Volume.class, pair.getTargetElement()).getConsistencyGroup();
            if (currentTargetCG == null) {
                throw APIException.badRequests.remoteReplicationOperationPrecheckFailed(String.format(
                        "remote replication pair %'s target volume has no consistency group", pair.getNativeId()));
            } else if (targetCG == null) {
                targetCG = currentTargetCG;
            } else if (!targetCG.equals(currentTargetCG)) {
                throw APIException.badRequests.remoteReplicationOperationPrecheckFailed(String.format(
                        "remote repliation pair %s's target volume's consistency group is not the same as others",
                        pair.getNativeId()));
            }
        }
    }

    TaskList processSrdfLinkRequest(RemoteReplicationOperationParam.OperationContext operationContext, List<URI> pairURIs) {
        // verify if this is supported for SRDF
        // delegate to BlockService method
        return null;
    }
}
