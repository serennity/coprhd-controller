/*
 * Copyright (c) 2012 EMC Corporation
 * All Rights Reserved
 */

package com.emc.storageos.systemservices;

import java.io.InputStream;
import java.util.List;

import com.emc.storageos.coordinator.client.model.SoftwareVersion;
import com.emc.storageos.coordinator.client.model.RepositoryInfo;

import com.emc.storageos.systemservices.impl.upgrade.LocalRepository;

import org.junit.Assert;
import org.junit.Test;

public class LocalRepositoryTest {
    @Test
    public void localRepositoryTest() throws Exception {
        // getVersions() returns an empty List but never null
        LocalRepository _localRepo = LocalRepository.getInstance();
        RepositoryInfo state = _localRepo.getRepositoryInfo();
        SoftwareVersion current = state.getCurrentVersion();
        Assert.assertNotNull(current);
        System.out.println("current=" + current);

        List<SoftwareVersion> available = state.getVersions();
        Assert.assertNotNull(available);
        System.out.println("available=");
        byte[] buf = new byte[100];
        for (SoftwareVersion v : available) {
            System.out.println(v);
            InputStream in = _localRepo.getImageInputStream(v);
            try {
                Assert.assertTrue(in.read(buf) == buf.length);
            } finally {
                in.close();
            }
        }
    }
}
