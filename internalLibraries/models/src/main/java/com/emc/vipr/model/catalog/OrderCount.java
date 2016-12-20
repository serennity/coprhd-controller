/*
 * Copyright (c) 2016 EMC Corporation
 * All Rights Reserved
 */

package com.emc.vipr.model.catalog;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "order_count")
public class OrderCount {
    private Map<String, Long> countMap = new HashMap();

    public OrderCount() {
    }

    public OrderCount(Map<String, Long> map) {
        countMap = map;
    }

    public void put(String key, long count) {
        countMap.put(key, count);
    }

    @XmlElement(name = "counts")
    public Map<String, Long> getCounts() {
        return countMap;
    }
}