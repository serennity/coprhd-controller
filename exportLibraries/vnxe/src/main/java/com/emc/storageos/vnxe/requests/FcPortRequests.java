/*
 * Copyright (c) 2014 EMC Corporation
 * All Rights Reserved
 */

package com.emc.storageos.vnxe.requests;

import java.util.List;

import com.emc.storageos.vnxe.models.VNXeFCPort;

public class FcPortRequests extends KHRequests<VNXeFCPort>{
    private static final String URL = "/api/types/fcPort/instances";
    
    public FcPortRequests(KHClient client) {
        super(client);
        _url = URL;
    }
    
    public List<VNXeFCPort> get() {
        return getDataForObjects(VNXeFCPort.class);
    }

}
