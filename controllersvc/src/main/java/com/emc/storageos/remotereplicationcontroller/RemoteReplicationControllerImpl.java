/*
 * Copyright (c) 2016 EMC Corporation
 * All Rights Reserved
 */
package com.emc.storageos.remotereplicationcontroller;


import com.emc.storageos.Controller;
import com.emc.storageos.db.client.DbClient;
import com.emc.storageos.db.client.model.DiscoveredSystemObject;
import com.emc.storageos.db.client.model.StorageSystem;
import com.emc.storageos.db.client.model.remotereplication.RemoteReplicationGroup;
import com.emc.storageos.db.client.util.NullColumnValueGetter;
import com.emc.storageos.filereplicationcontroller.FileReplicationController;
import com.emc.storageos.impl.AbstractDiscoveredSystemController;
import com.emc.storageos.svcs.errorhandling.resources.InternalException;
import com.emc.storageos.volumecontroller.ControllerException;
import com.emc.storageos.volumecontroller.impl.Dispatcher;

import java.net.URI;
import java.util.List;
import java.util.Set;

public class RemoteReplicationControllerImpl implements RemoteReplicationController {

    private final static String REMOTE_REPLICATION_DEVICE = "remote-replication-device";
    private Set<RemoteReplicationController> deviceControllers;
    private Dispatcher dispatcher;
    private DbClient dbClient;

    public Set<RemoteReplicationController> getDeviceControllers() {
        return deviceControllers;
    }

    public void setDeviceControllers(Set<RemoteReplicationController> deviceControllers) {
        this.deviceControllers = deviceControllers;
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    public void setDispatcher(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public DbClient getDbClient() {
        return dbClient;
    }

    public void setDbClient(DbClient dbClient) {
        this.dbClient = dbClient;
    }

    protected Controller getController() {
        return deviceControllers.iterator().next();
    }

    private void exec(String methodName, Object... args) throws ControllerException {
            dispatcher.queue(NullColumnValueGetter.getNullURI(), REMOTE_REPLICATION_DEVICE,
                    getController(), methodName, args);
    }

    @Override
    public void createRemoteReplicationGroup(URI replicationGroup, String opId) {
        RemoteReplicationGroup rrGroup = dbClient.queryObject(RemoteReplicationGroup.class, replicationGroup);
        exec("createRemoteReplicationGroup", replicationGroup, opId);

    }

    @Override
    public void createGroupReplicationPairs(List<URI> replicationPairs, String opId) {

    }

    @Override
    public void createSetReplicationPairs(List<URI> replicationPairs, String opId) {

    }

    @Override
    public void deleteReplicationPairs(List<URI> replicationPairs, String opId) {

    }

    @Override
    public void suspend(URI replicationArgument, String opId) {

    }

    @Override
    public void resume(URI replicationArgument, String opId) {

    }

    @Override
    public void split(URI replicationArgument, String opId) {

    }

    @Override
    public void establish(URI replicationArgument, String opId) {

    }

    @Override
    public void failover(URI replicationArgument, String opId) {

    }

    @Override
    public void failback(URI replicationArgument, String opId) {

    }

    @Override
    public void swap(URI replicationArgument, String opId) {

    }

    @Override
    public void movePair(URI replicationPair, URI targetGroup, String opId) {

    }
}