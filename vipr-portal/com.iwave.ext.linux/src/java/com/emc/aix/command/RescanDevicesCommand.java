/*
 * Copyright (c) 2012-2015 iWave Software LLC
 * All Rights Reserved
 */
package com.emc.aix.command;

import org.apache.commons.lang.StringUtils;

import com.iwave.ext.command.CommandException;

public class RescanDevicesCommand extends AixCommand {

    public RescanDevicesCommand() {
        setCommand("cfgmgr");
        setRunAsRoot(true);
    }

    @Override
    protected void processError() throws CommandException {
        String stdout = StringUtils.trimToEmpty(getOutput().getStdout());
        String stderr = StringUtils.trimToEmpty(getOutput().getStderr());
        if (stdout.isEmpty() || !stderr.isEmpty()) {
            super.processError();
        }
    }

}
