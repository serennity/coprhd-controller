/*
 * Copyright (c) 2015 EMC Corporation
 * All Rights Reserved
 */
package com.emc.storageos.db.client.model;

import java.net.URI;

/**
 * 
 * @author jainm15
 *
 */
@Cf("FilePolicy")
public class FilePolicy extends DataObjectWithACLs {

    private static final long serialVersionUID = 1L;

    // Type of the policy
    private String filePolicyType;

    // Name of the policy
    private String filePolicyName;

    // Description of the policy
    private String filePolicyDescription;

    // Level at which policy has to be applied..
    private String applyAt;

    // Assigned resources for which policy is being applied
    private StringSet assignedResources;

    // Tenants who will have access to this policy
    private StringSet accessTenants;

    // Type of schedule policy e.g days, weeks or months
    private String scheduleFrequency;

    // Policy run on every
    private Long scheduleRepeat;

    // Time when policy run
    private String scheduleTime;

    // Day of week when policy run
    private String scheduleDayOfWeek;

    // Day of month when policy run
    private Long scheduleDayOfMonth;

    // Snapshot expire type e.g hours, days, weeks, months or never
    private String snapshotExpireType;

    // Snapshot expire at
    private Long snapshotExpireTime;

    // File Replication type
    private String fileReplicationType;

    // File Replication copy type
    private String fileReplicationCopyMode;

    // File Policy associated Virtual Pool - this is being used for project and file system policy
    private URI filePolicyVpool;

    /**
     * TRUE means: if policy has to be applied on all file system coming under specified vpool, at the time of
     * provisioning.
     * FALSE means : policy has to applied on the specific file system chosen at the time of provisioning..
     */
    private Boolean applyToAllFS;

    public static enum FileReplicationType {
        LOCAL, REMOTE;
    }

    public static enum FileReplicationCopyMode {
        SYNC, ASYNC;
    }

    public static enum ScheduleFrequency {
        DAYS, WEEKS, MONTHS
    }

    public static enum SnapshotExpireType {
        HOURS, DAYS, WEEKS, MONTHS, NEVER
    }

    public static enum FilePolicyType {
        file_snapshot, file_replication, file_quota
    }

    public static enum FilePolicyApplyLevel {
        vpool, project, file_system
    }

    @Name("fileReplicationType")
    public String getFileReplicationType() {
        return this.fileReplicationType;
    }

    public void setFileReplicationType(String fileReplicationType) {
        this.fileReplicationType = fileReplicationType;
        setChanged("fileReplicationType");
    }

    @Name("frCopyMode")
    public String getFileReplicationCopyMode() {
        return this.fileReplicationCopyMode;
    }

    public void setFileReplicationCopyMode(String fileReplicationCopyMode) {
        this.fileReplicationCopyMode = fileReplicationCopyMode;
        setChanged("frCopyMode");
    }

    @Name("filePolicyType")
    public String getFilePolicyType() {
        return this.filePolicyType;
    }

    public void setFilePolicyType(String filePolicyType) {
        this.filePolicyType = filePolicyType;
        setChanged("filePolicyType");
    }

    @Name("filePolicyName")
    public String getFilePolicyName() {
        return this.filePolicyName;
    }

    public void setFilePolicyName(String filePolicyName) {
        this.filePolicyName = filePolicyName;
        setChanged("filePolicyName");
    }

    @Name("filePolicyDescription")
    public String getFilePolicyDescription() {
        return this.filePolicyDescription;
    }

    public void setFilePolicyDescription(String filePolicyDescription) {
        this.filePolicyDescription = filePolicyDescription;
        setChanged("filePolicyDescription");
    }

    @Name("applyAt")
    public String getApplyAt() {
        return this.applyAt;
    }

    public void setApplyAt(String applyAt) {
        this.applyAt = applyAt;
        setChanged("applyAt");
    }

    @Name("scheduleFrequency")
    public String getScheduleFrequency() {
        return this.scheduleFrequency;
    }

    public void setScheduleFrequency(String scheduleFrequency) {
        this.scheduleFrequency = scheduleFrequency;
        setChanged("scheduleFrequency");
    }

    @Name("scheduleRepeat")
    public Long getScheduleRepeat() {
        return this.scheduleRepeat;
    }

    public void setScheduleRepeat(Long scheduleRepeat) {
        this.scheduleRepeat = scheduleRepeat;
        setChanged("scheduleRepeat");
    }

    @Name("scheduleTime")
    public String getScheduleTime() {
        return this.scheduleTime;
    }

    public void setScheduleTime(String scheduleTime) {
        this.scheduleTime = scheduleTime;
        setChanged("scheduleTime");
    }

    @Name("scheduleDayOfWeek")
    public String getScheduleDayOfWeek() {
        return this.scheduleDayOfWeek;
    }

    public void setScheduleDayOfWeek(String scheduleDayOfWeek) {
        this.scheduleDayOfWeek = scheduleDayOfWeek;
        setChanged("scheduleDayOfWeek");
    }

    @Name("scheduleDayOfMonth")
    public Long getScheduleDayOfMonth() {
        return this.scheduleDayOfMonth;
    }

    public void setScheduleDayOfMonth(Long scheduleDayOfMonth) {
        this.scheduleDayOfMonth = scheduleDayOfMonth;
        setChanged("scheduleDayOfMonth");
    }

    @Name("snapshotExpireType")
    public String getSnapshotExpireType() {
        return this.snapshotExpireType;
    }

    public void setSnapshotExpireType(String snapshotExpireType) {
        this.snapshotExpireType = snapshotExpireType;
        setChanged("snapshotExpireType");
    }

    @Name("snapshotExpireTime")
    public Long getSnapshotExpireTime() {
        return this.snapshotExpireTime;
    }

    public void setSnapshotExpireTime(Long snapshotExpireTime) {
        this.snapshotExpireTime = snapshotExpireTime;
        setChanged("snapshotExpireTime");
    }

    @Name("accessTenants")
    public StringSet getTenantOrg() {
        return this.accessTenants;
    }

    public void setTenantOrg(StringSet accessTenants) {
        this.accessTenants = accessTenants;
        setChanged("accessTenants");
    }

    @Name("assignedResources")
    public StringSet getAssignedResources() {
        return this.assignedResources;
    }

    public void setAssignedResources(StringSet assignedResources) {
        this.assignedResources = assignedResources;
        setChanged("assignedResources");
    }

    @Name("applyToAllFS")
    public Boolean isApplyToAllFS() {
        return this.applyToAllFS;
    }

    public void setApplyToAllFS(Boolean applyToAllFS) {
        this.applyToAllFS = applyToAllFS;
        setChanged("applyToAllFS");
    }

    @Name("filePolicyVpool")
    public URI getFilePolicyVpool() {
        return this.filePolicyVpool;
    }

    public void setFilePolicyVpool(URI filePolicyVpool) {
        this.filePolicyVpool = filePolicyVpool;
        setChanged("filePolicyVpool");
    }
}