/*
 * Copyright (c) 2016 EMC
 * All Rights Reserved
 */
package com.emc.sa.service.vipr.application.tasks;

import java.net.URI;
import java.util.List;

import com.emc.sa.service.vipr.tasks.WaitForTasks;
import com.emc.storageos.model.TaskList;
import com.emc.storageos.model.TaskResourceRep;
import com.emc.storageos.model.application.VolumeGroupSnapshotSessionCreateParam;
import com.emc.vipr.client.Tasks;

public class CreateSnapshotSessionForApplication extends WaitForTasks<TaskResourceRep> {
    private final URI applicationId;
    private final String name;
    private final List<URI> volumes;

    public CreateSnapshotSessionForApplication(URI applicationId, List<URI> volumes, String name) {
        this.applicationId = applicationId;
        this.name = name;
        this.volumes = volumes;
        provideDetailArgs(applicationId, name);
    }

    @Override
    protected Tasks<TaskResourceRep> doExecute() throws Exception {
        VolumeGroupSnapshotSessionCreateParam input = new VolumeGroupSnapshotSessionCreateParam();
        input.setName(name);
        input.setVolumes(volumes);
        input.setPartial(true);

        TaskList taskList = getClient().application().createSnapshotSessionOfApplication(applicationId, input);

        return new Tasks<TaskResourceRep>(getClient().auth().getClient(), taskList.getTaskList(),
                TaskResourceRep.class);
    }
}
