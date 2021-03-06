/*
 * Copyright (c) 2012-2015 iWave Software LLC
 * All Rights Reserved
 */
package com.emc.sa.service.vipr.block.tasks;

import java.net.URI;

import org.apache.commons.lang.StringUtils;

import com.emc.sa.asset.providers.BlockProvider;
import com.emc.sa.service.vipr.tasks.WaitForTasks;
import com.emc.storageos.model.block.BlockSnapshotRestRep;
import com.emc.storageos.model.block.VolumeSnapshotParam;
import com.emc.vipr.client.Tasks;

public class CreateBlockSnapshot extends WaitForTasks<BlockSnapshotRestRep> {
    private URI volumeId;
    private String name;
    private String type;
    private Boolean readOnly;

    public CreateBlockSnapshot(String volumeId, String type, String name, Boolean readOnly) {
        this(uri(volumeId), type, name, readOnly);
    }

    public CreateBlockSnapshot(URI volumeId, String type, String name, Boolean readOnly) {
        this.volumeId = volumeId;
        this.type = (type == null) ? "" : type;
        this.name = name;
        this.readOnly = readOnly;
        provideDetailArgs(volumeId, this.type, name, readOnly);
    }

    @Override
    protected Tasks<BlockSnapshotRestRep> doExecute() throws Exception {
        VolumeSnapshotParam snapshot = new VolumeSnapshotParam();
        if (StringUtils.isNotBlank(type) && !type.equals(BlockProvider.LOCAL_ARRAY_SNAPSHOT_TYPE_VALUE)) {
            snapshot.setType(type);
        }
        if (readOnly != null) {
            snapshot.setReadOnly(readOnly);
        }
        snapshot.setName(name);
        return getClient().blockSnapshots().createForVolume(volumeId, snapshot);
    }
}
