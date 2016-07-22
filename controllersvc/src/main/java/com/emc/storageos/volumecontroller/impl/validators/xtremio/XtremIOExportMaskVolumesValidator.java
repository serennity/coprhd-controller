/*
 * Copyright (c) 2016 EMC Corporation
 * All Rights Reserved
 */
package com.emc.storageos.volumecontroller.impl.validators.xtremio;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.storageos.db.client.model.ExportMask;
import com.emc.storageos.db.client.model.StorageSystem;
import com.emc.storageos.db.client.model.Volume;
import com.emc.storageos.exceptions.DeviceControllerException;
import com.emc.storageos.volumecontroller.impl.validators.DefaultValidator;
import com.emc.storageos.volumecontroller.impl.xtremio.prov.utils.XtremIOProvUtils;
import com.emc.storageos.xtremio.restapi.XtremIOClient;
import com.emc.storageos.xtremio.restapi.model.response.XtremIOVolume;

/**
 * Validator class for XtremIO export mask delete operations.
 */
public class XtremIOExportMaskVolumesValidator extends AbstractXtremIOValidator {

    private static final Logger log = LoggerFactory.getLogger(XtremIOExportMaskVolumesValidator.class);

    private final Collection<URI> volumeURIs;
    private Collection<String> igNames;

    public XtremIOExportMaskVolumesValidator(StorageSystem storage, ExportMask exportMask,
            Collection<URI> volumeURIList) {
        super(storage, exportMask);
        this.volumeURIs = volumeURIList;
    }

    public void setIgNames(Collection<String> igNames) {
        this.igNames = igNames;
    }

    @Override
    public boolean validate() throws Exception {
        log.info("Initiating volume validation of XtremIO ExportMask: " + id);
        try {
            XtremIOClient client = XtremIOProvUtils.getXtremIOClient(getDbClient(), storage, getClientFactory());
            String xioClusterName = client.getClusterDetails(storage.getSerialNumber()).getName();
            Set<String> knownVolumes = new HashSet<String>();
            Set<String> igVols = new HashSet<String>();
            // get the volumes in the IGs and validate against passed impacted volumes
            List<Volume> knownVolumesInIG = getDbClient().queryObject(Volume.class, volumeURIs);
            List<XtremIOVolume> igVolumes = new ArrayList<XtremIOVolume>();
            for (String igName : igNames) {
                igVolumes.addAll(XtremIOProvUtils.getInitiatorGroupVolumes(igName, xioClusterName, client));
            }
            for (Volume knownVolumeInIG : knownVolumesInIG) {
                knownVolumes.add(knownVolumeInIG.getDeviceLabel());
            }

            for (XtremIOVolume igVolume : igVolumes) {
                igVols.add(igVolume.getVolInfo().get(1));
            }

            log.info("ViPR known volumes present in IG: {}, volumes in IG: {}", knownVolumes, igVols);
            igVols.removeAll(knownVolumes);
            for (String igVol : igVols) {
                getLogger().logDiff(id, "volumes", NO_MATCH, igVol);
            }
        } catch (Exception ex) {
            log.error("Unexpected exception validating ExportMask volumes: " + ex.getMessage(), ex);
            if (DefaultValidator.validationEnabled(getCoordinator())) {
                throw DeviceControllerException.exceptions.unexpectedCondition(
                        "Unexpected exception validating ExportMask volumes: " + ex.getMessage());
            }
        }

        checkForErrors();

        log.info("Completed volume validation of XtremIO ExportMask: " + id);

        return true;
    }

}
