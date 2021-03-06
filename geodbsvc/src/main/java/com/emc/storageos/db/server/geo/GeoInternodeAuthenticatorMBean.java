/*
 * Copyright (c) 2014 EMC Corporation
 * All Rights Reserved
 */

package com.emc.storageos.db.server.geo;

import java.util.List;

/**
 * The API for blacklist
 */
public interface GeoInternodeAuthenticatorMBean {
    static final String MBEAN_NAME = "com.emc.storageos.db.server.geo:type=GeoInternodeAuthenticator";

    /**
     * Add node IPs to blacklist
     * 
     * @param newBlacklist
     */
    void addToBlacklist(List<String> newBlacklist);

    /**
     * Remove give list of node IP from blacklist
     * 
     * @param nodeList
     */
    void removeFromBlacklist(List<String> nodeList);

    /**
     * Get list of node IP in blacklist
     * 
     * @return
     */
    List<String> getBlacklist();

}
