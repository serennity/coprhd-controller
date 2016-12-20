/*
 * Copyright (c) 2016 EMC Corporation
 * All Rights Reserved
 */
package com.emc.storageos.volumecontroller.impl.externaldevice.taskcompleters;

import static java.util.Arrays.asList;

import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.storageos.db.client.DbClient;
import com.emc.storageos.db.client.model.Operation;
import com.emc.storageos.db.client.model.Volume;
import com.emc.storageos.exceptions.DeviceControllerException;
import com.emc.storageos.svcs.errorhandling.model.ServiceCoded;
import com.emc.storageos.volumecontroller.TaskCompleter;

public class RemoteReplicationTaskCompleter extends TaskCompleter {

    private static final Logger _logger = LoggerFactory.getLogger(RemoteReplicationTaskCompleter.class);

    private DbClient dbClient;

    protected void setDbClient(DbClient dbClient) {
        this.dbClient = dbClient;
    }

    protected DbClient getDbClient() {
        return dbClient;
    }

    /**
     * Constructor for specifying a combination of source and multiple target ID's.
     *
     * @param ids
     * @param opId
     */
    public RemoteReplicationTaskCompleter(List<URI> ids, String opId) {
        super(Volume.class, ids, opId);
    }

    public RemoteReplicationTaskCompleter(URI sourceURI, URI targetURI, String opId) {
        super(Volume.class, asList(sourceURI, targetURI), opId);
    }

    @Override
    protected void complete(DbClient dbClient, Operation.Status status, ServiceCoded coded)
            throws DeviceControllerException {
        setDbClient(dbClient);
        setStatus(dbClient, status, coded);
        updateWorkflowStatus(status, coded);
    }

}
