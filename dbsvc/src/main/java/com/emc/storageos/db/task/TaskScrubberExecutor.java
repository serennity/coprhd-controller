/*
 * Copyright (c) 2015 EMC Corporation
 * All Rights Reserved
 */
package com.emc.storageos.db.task;

import java.net.URI;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.storageos.coordinator.client.service.CoordinatorClient;
import com.emc.storageos.db.client.DbClient;
import com.emc.storageos.db.client.model.Task;
import com.emc.storageos.services.util.NamedScheduledThreadPoolExecutor;
import com.google.common.collect.Lists;
import com.netflix.astyanax.util.TimeUUIDUtils;

/**
 * Deletes completed tasks that have completed and are over {@link #TASK_TTL_MINS_PROPERTY} old at periodic intervals
 */
public class TaskScrubberExecutor {
    private static final Logger log = LoggerFactory.getLogger(TaskScrubberExecutor.class);
    private static final String TASK_TTL_MINS_PROPERTY = "task_ttl";
    private static final String TASK_CLEAN_INTERVAL_PROPERTY = "task_clean_interval";

    private final static long MIN_TO_MICROSECS = 60 * 1000 * 1000;
    private final static long MINI_TO_MICROSECS = 1000;
    private final static long MINIMUM_PERIOD_MINS = 60;
    private final static int DELETE_BATCH_SIZE = 100;
    private final static int MAXIMUM_TASK_TO_DELETE = 10 * DELETE_BATCH_SIZE;
    
    private final static String TASK_SCRUBBER_LOCK = "task_scrubber_lock";

    private ScheduledExecutorService _executor = new NamedScheduledThreadPoolExecutor("TaskScrubber", 1);
    private DbClient dbClient;
    private CoordinatorClient coordinator;
    private InterProcessLock lock = null;

    public void start() {
        _executor.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                deleteOldTasks();
            }
        }, 10, getConfigProperty(TASK_CLEAN_INTERVAL_PROPERTY, MINIMUM_PERIOD_MINS), TimeUnit.MINUTES);
        log.info("Started Task Scrubber to run every {} minutes", getConfigProperty(TASK_CLEAN_INTERVAL_PROPERTY, MINIMUM_PERIOD_MINS));
    }

    public DbClient getDbClient() {
        return dbClient;
    }

    public void setDbClient(DbClient dbClient) {
        this.dbClient = dbClient;
    }

    public CoordinatorClient getCoordinator() {
        return coordinator;
    }

    public void setCoordinator(CoordinatorClient coordinator) {
        this.coordinator = coordinator;
    }

    private void deleteOldTasks() {
        boolean lockAcquired = false;
        try {
            
            // acquire a lock so clean up is only done on one node
            lockAcquired = acquireLock();
            
            if (lockAcquired) {
                log.info("Looking for completed tasks older than {} minutes", getConfigProperty(TASK_TTL_MINS_PROPERTY, MINIMUM_PERIOD_MINS));
        
                long taskLifetimeMicroSeconds = getConfigProperty(TASK_TTL_MINS_PROPERTY, MINIMUM_PERIOD_MINS) * MIN_TO_MICROSECS;
                long currentTimeMicroseconds = TimeUUIDUtils.getMicrosTimeFromUUID(TimeUUIDUtils.getUniqueTimeUUIDinMicros());
                long startTimeMicroSec = currentTimeMicroseconds - taskLifetimeMicroSeconds;
                Calendar startTimeMarker = Calendar.getInstance();
                startTimeMarker.setTimeInMillis(startTimeMicroSec/MINI_TO_MICROSECS);
                
                int tasksDeleted = 0;
        
                List<URI> ids = dbClient.queryByType(Task.class, true);
                Iterator<Task> tasks = dbClient.queryIterativeObjects(Task.class, ids, true);
                List<Task> toBeDeleted = Lists.newArrayList();
                while (tasks.hasNext()) {
                    Task task = tasks.next();
                    if (task.getCreationTime().after(startTimeMarker)) {
                    	continue;
                    }
                    if (task != null && !task.isPending()) {
                        tasksDeleted++;
                        toBeDeleted.add(task);
                    }
                    if (toBeDeleted.size() >= DELETE_BATCH_SIZE) {
                    	log.info("Deleting {} Tasks", toBeDeleted.size());
                    	dbClient.markForDeletion(toBeDeleted);
                    	toBeDeleted.clear();
                    }
                    if (tasksDeleted >= MAXIMUM_TASK_TO_DELETE) {
                        break;
                    }
                }
        
                if (!toBeDeleted.isEmpty()) {
                    log.info("Deleting {} Tasks", toBeDeleted.size());
        
                    dbClient.markForDeletion(toBeDeleted);
                } 
                log.info("delete completed tasks successfully; deleted {} tasks", tasksDeleted);
            }
        } finally {
            if (lockAcquired) {
                releaseLock();
            }
        }
    }
    
    private boolean acquireLock() {
        boolean acquired = false;
        try {
            log.info("Attempting to acquire distributed lock {}", TASK_SCRUBBER_LOCK);
            lock = coordinator.getLock(TASK_SCRUBBER_LOCK);
            acquired = lock.acquire(0, TimeUnit.SECONDS);
            if (acquired) {
                log.info("Successfully acquired distributed lock {}", TASK_SCRUBBER_LOCK);
            } else {
                log.info("Failed to acquire distributed lock {}", TASK_SCRUBBER_LOCK);
            }
        } catch (Exception e) {
            log.info("Exception while attempting to acquire distributed lock {}", TASK_SCRUBBER_LOCK);
            log.error(e.getMessage(), e);
        }
        return acquired;
    }
    
    private void releaseLock() {
        log.info("Attempting to release distributed lock {}", TASK_SCRUBBER_LOCK);
        try {
            lock.release();
            log.info("Successfully released distributed lock {}", TASK_SCRUBBER_LOCK);
        } catch (Exception e) {
            log.info("Failed to release distributed lock {}", TASK_SCRUBBER_LOCK);
            log.error(e.getMessage(), e);
        }
    }

    private long getConfigProperty(String propertyName, long minimumValue) {
        String value = coordinator.getPropertyInfo().getProperty(propertyName);

        if (value != null && StringUtils.isNotBlank(value)) {
            try {
                return Math.max(Long.valueOf(value), minimumValue);
            } catch (Exception e) {
                log.error("Configuration property " + propertyName + " invalid number, using minimum value of " + minimumValue, e);
                return minimumValue;
            }
        } else {
            log.error("Configuration property " + propertyName + " not found, using minimum value of " + minimumValue);
            return minimumValue;
        }
    }
}
