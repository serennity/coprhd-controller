/*
 * Copyright (c) 2012 EMC Corporation
 * All Rights Reserved
 */
package com.emc.vipr.model.sys.ipreconfig;

import com.emc.storageos.model.property.PropertyConstants;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import java.io.*;
import java.util.*;

/**
 * Cluster IP Information
 * Each cluster could include multiple physical sites
 */
@XmlRootElement(name = "cluster_ipinfo")
public class ClusterIpInfo implements Serializable {

    private Map<String, SiteIpInfo> siteIpInfoMap = new HashMap<String, SiteIpInfo>();

    public ClusterIpInfo() {
    }

    @XmlElementWrapper(name="sites")
    public Map<String, SiteIpInfo> getSiteIpInfoMap() {
        return siteIpInfoMap;
    }

    public void setSiteIpInfoMap(Map<String, SiteIpInfo> siteIpInfoMap) {
        this.siteIpInfoMap = siteIpInfoMap;
    }

    public byte[] serialize() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        try {
            out.writeObject(this);
        } finally {
            out.close();
        }
        return bos.toByteArray();
    }

    public static ClusterIpInfo deserialize(byte[] data) throws IOException,
            ClassNotFoundException {
        Object obj = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream in = new ObjectInputStream(bis);
        try {
            obj = in.readObject();
        } finally {
            in.close();
        }
        return (ClusterIpInfo) obj;
    }

    /* (non-Javadoc)
   	 * @see java.lang.Object#hashCode()
   	 */
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((siteIpInfoMap == null) ? 0 : siteIpInfoMap.hashCode());
		return result;
	}

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        if (!siteIpInfoMap.equals(((ClusterIpInfo) obj).getSiteIpInfoMap())) {
            return false;
        }

        return true;
    }


	@Override
    public String toString() {
        StringBuffer propStrBuf = new StringBuffer();

        SortedSet<String> keys = new TreeSet<String>(siteIpInfoMap.keySet());
        for (String key : keys) {
            propStrBuf.append(key);
            propStrBuf.append(PropertyConstants.DELIMITER);
            propStrBuf.append(siteIpInfoMap.get(key).toString());
            propStrBuf.append("\n");
        }

        return propStrBuf.toString();
    }

    /*
     * Load key/value property map
     *
     */
    public void loadFromPropertyMap(Map<String, String> globalPropMap)
    {
        /* TODO: change vdc site property prefix to vdc_<vdcshortId>_<siteShortId>
        e.g. vdc_vdc1_site2_network_1_ipaddr */

        // group global properties in site level - "<vdcsiteId> -> <vdcsiteInternalProperties>"
        Map<String, Map<String, String>> vdcsitePropMap = new HashMap<String, Map<String, String>>();
        SortedSet<String> globalPropNames = new TreeSet<String>(globalPropMap.keySet());
        for (String globalPropName : globalPropNames) {
            // separate global prop name to vdcsiteId and internal prop name;
            String[] tmpFields = globalPropName.split("_");
            String vdcsiteId = tmpFields[0] + "_" + tmpFields[1] + "_" + tmpFields[2];
            String vdcsiteInternalPropName = globalPropName.split(vdcsiteId)[1];

            // put globalPropMap into vdcsitePropMap;
            Map<String, String> sitePropMap = vdcsitePropMap.get(vdcsiteId);
            if (null == sitePropMap) {
                sitePropMap = new HashMap<String, String>();
            }
            sitePropMap.put(vdcsiteInternalPropName, globalPropMap.get(globalPropName));
            vdcsitePropMap.put(vdcsiteId, sitePropMap);
        }

        // load all the site level properties
        SortedSet<String> vdcsiteIds = new TreeSet<String>(vdcsitePropMap.keySet());
        for (String vdcsiteId : vdcsiteIds) {
            SiteIpInfo siteIpInfo = siteIpInfoMap.get(vdcsiteId);
            if (null == siteIpInfo) {
                siteIpInfo = new SiteIpInfo();
            }
            siteIpInfo.loadFromPropertyMap(vdcsitePropMap.get(vdcsiteId));
            siteIpInfoMap.put(vdcsiteId, siteIpInfo);
        }
    }

    public String validate(int nodecount) {
        String errmsg = "";
        /* TODO:
        if (ipv4_setting.isDefault() && ipv6_setting.isDefault()) {
            errmsg = "Both IPv4 and IPv6 networks are not configured.";
            return errmsg;
        }

        if (!ipv4_setting.isValid()) {
            errmsg = "IPv4 adresses are not valid.";
            return errmsg;
        }

        if (!ipv6_setting.isValid()) {
            errmsg = "IPv6 adresses are not valid.";
            return errmsg;
        }

        if (ipv4_setting.isDuplicated()) {
            errmsg = "IPv4 adresses are duplicated.";
            return errmsg;
        }

        if (ipv6_setting.isDuplicated()) {
            errmsg = "IPv6 adresses are duplicated.";
            return errmsg;
        }

        if (!ipv4_setting.isOnSameNetworkIPv4()) {
            errmsg = "IPv4 adresses are not in same network.";
            return errmsg;
        }

        if (!ipv6_setting.isOnSameNetworkIPv6()) {
            errmsg = "IPv6 adresses are not in same network.";
            return errmsg;
        }

        if (ipv4_setting.getNetworkAddrs().size() != ipv6_setting.getNetworkAddrs().size()) {
            errmsg = "Nodes number does not match between IPv4 and IPv6.";
            return errmsg;
        }

        if (ipv4_setting.getNetworkAddrs().size() != nodecount) {
            errmsg = "Nodes number does not match with the cluster.";
            return errmsg;
        }
        */
        return errmsg;
    }
}
