/*
 * Copyright 2015 EMC Corporation
 * All Rights Reserved
 */
package com.emc.sa.service.vipr.block.consistency.tasks;

import java.net.URI;

import com.emc.sa.service.vipr.tasks.WaitForTasks;
import com.emc.storageos.model.block.BlockConsistencyGroupRestRep;
import com.emc.vipr.client.Tasks;

public class DeactivateConsistencyGroupSnapshot extends
        WaitForTasks<BlockConsistencyGroupRestRep> {

    private URI consistencyGroup;
    private URI snapshot;

    public DeactivateConsistencyGroupSnapshot(URI consistencyGroup, URI snapshot) {
        this.consistencyGroup = consistencyGroup;
        this.snapshot = snapshot;
    }

    @Override
    protected Tasks<BlockConsistencyGroupRestRep> doExecute() throws Exception {
        return getClient().blockConsistencyGroups().deactivateSnapshot(consistencyGroup, snapshot);
    }
}
