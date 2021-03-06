package com.emc.storageos.ecs.api;

import com.emc.storageos.svcs.errorhandling.model.ExceptionMessagesProxy;
import com.emc.storageos.svcs.errorhandling.resources.InternalException;
import com.emc.storageos.svcs.errorhandling.resources.ServiceCode;

public class ECSException extends InternalException {
    private static final long serialVersionUID = 8903079831758201184L;

    /** Holds the methods used to create ECS related exceptions */
    public static final ECSExceptions exceptions = ExceptionMessagesProxy.create(ECSExceptions.class);

    private ECSException(final ServiceCode code, final Throwable cause,
            final String detailBase, final String detailKey, final Object[] detailParams) {
        super(false, code, cause, detailBase, detailKey, detailParams);
    }
}
