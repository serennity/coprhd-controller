/*
 * Copyright (c) 2015 EMC Corporation
 * All Rights Reserved
 */
package com.emc.hpux.command;

import static org.easymock.EasyMock.createMockBuilder;

import java.util.List;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.iwave.ext.command.CommandOutput;
import com.iwave.ext.linux.model.IPInterface;

public class ListIpInterfacesCommandTest {

    static String output = "lo0: flags=849<UP,LOOPBACK,RUNNING,MULTICAST>" + "\n" +
            "inet 127.0.0.1 netmask ff000000 " + "\n" +
            "\n" +
            "\n" +
            "lan0: flags=1843<UP,BROADCAST,RUNNING,MULTICAST,CKO>" + "\n" +
            "        inet 10.247.63.17 netmask ffffff00 broadcast 10.247.63.255" + "\n" +
            "\n" +
            "\n" +
            "lan0: flags=1843<UP,BROADCAST,RUNNING,MULTICAST,CKO>" + "\n" +
            "        inet 10.247.63.17 netmask ffffff00 broadcast 10.247.63.255" + "\n" +
            "\n" +
            "\n" +
            "lo0: flags=849<UP,LOOPBACK,RUNNING,MULTICAST>" + "\n" +
            "        inet 127.0.0.1 netmask ff000000 " + "\n" +
            "\n" +
            "\n" +
            "lan0: flags=1843<UP,BROADCAST,RUNNING,MULTICAST,CKO>" + "\n" +
            "       inet 10.247.63.17 netmask ffffff00 broadcast 10.247.63.255" + "\n";

    static ListIPInterfacesCommand ipInterfacesCommand = null;

    @BeforeClass
    public synchronized static void setup() {
        CommandOutput commandOutput = new CommandOutput(output, null, 0);
        ipInterfacesCommand = createMockBuilder(ListIPInterfacesCommand.class).withConstructor().addMockedMethod("getOutput").createMock();
        EasyMock.expect(ipInterfacesCommand.getOutput()).andReturn(commandOutput).anyTimes();
        EasyMock.replay(ipInterfacesCommand);
    }

    @Test
    public void testCommand() {
        ipInterfacesCommand.parseOutput();
        List<IPInterface> results = ipInterfacesCommand.getResults();
        Assert.assertEquals(5, results.size());
        for (IPInterface ip : results) {
            System.out.println(ip);
        }
    }

}
