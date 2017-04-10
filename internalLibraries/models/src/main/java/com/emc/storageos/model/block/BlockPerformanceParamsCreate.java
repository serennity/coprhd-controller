/*
 * Copyright 2017 EMC Corporation
 * All Rights Reserved
 */
package com.emc.storageos.model.block;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.emc.storageos.model.valid.Length;

/**
 * Parameters to create a block PerformanceParams instance.
 */
@XmlRootElement(name = "block_performance_params_create")
public class BlockPerformanceParamsCreate {

    // A unique user supplied name.
    private String name;

    // A user supplied description.
    private String description;

    // The FAST Policy Name.
    private String autoTieringPolicyName;

    // Compression setting for all Flash VMAX3 storage arrays.
    private Boolean compressionEnabled;

    // VMAX Host I/O bandwidth limit. Unlimited when not specified or 0.
    private Integer hostIOLimitBandwidth;

    // VMAX Host I/O IOPS limit. Unlimited when not specified or 0.
    private Integer hostIOLimitIOPs;

    // Percentage of the requested volume size that is allocated when provisioning thin volumes.
    private Integer thinVolumePreAllocationPercentage;

    // Whether or not volumes should be provisioned in storage pools that support deduplication.
    private Boolean dedupCapable;

     // Whether or not to use striped (false) or concatenated (true) meta-volumes when expanding volumes.
    private Boolean fastExpansion;

    /**
     * Default constructor
     */
    public BlockPerformanceParamsCreate()
    {}
    
    /*
     * Required Setters and Getters
     */

    /**
     * The unique name for the performance parameters instance.
     */
    @XmlElement(required = true)
    @Length(min = 2, max = 128)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The description for the performance parameters instance.
     */
    @XmlElement(required = false)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The FAST policy name for the performance parameters instance.
     */
    @XmlElement(name = "auto_tiering_policy_name", required = false)
    public String getAutoTieringPolicyName() {
        return autoTieringPolicyName;
    }

    public void setAutoTieringPolicyName(String autoTieringPolicyName) {
        this.autoTieringPolicyName = autoTieringPolicyName;
    }
    
    /**
     * The compression setting for the performance parameters instance.
     */
    @XmlElement(name = "compression_enabled", required = false)
    public Boolean getCompressionEnabled() {
        return compressionEnabled;
    }

    public void setCompressionEnabled(Boolean compressionEnabled) {
        this.compressionEnabled = compressionEnabled;
    }

    /**
     * The host I/O bandwidth limit for the performance parameters instance.
     */
    @XmlElement(name = "host_io_limit_bandwidth", required = false)
    public Integer getHostIOLimitBandwidth() {
        return hostIOLimitBandwidth;
    }

    public void setHostIOLimitBandwidth(Integer hostIOLimitBandwidth) {
        this.hostIOLimitBandwidth = hostIOLimitBandwidth;
    }

    /**
     * The host I/O IOPS limit for the performance parameters instance.
     */
    @XmlElement(name = "host_io_limit_iops", required = false)
    public Integer getHostIOLimitIOPs() {
        return hostIOLimitIOPs;
    }

    public void setHostIOLimitIOPs(Integer hostIOLimitIOPs) {
        this.hostIOLimitIOPs = hostIOLimitIOPs;
    }

    /**
     * The thin volume pre-allocation percentage for the performance parameters instance.
     */
    @XmlElement(name = "thin_volume_preallocation_percentage", required = false)
    public Integer getThinVolumePreAllocationPercentage() {
        return thinVolumePreAllocationPercentage;
    }

    public void setThinVolumePreAllocationPercentage(
            Integer thinVolumePreAllocationPercentage) {
        this.thinVolumePreAllocationPercentage = thinVolumePreAllocationPercentage;
    }

    /**
     * The deduplication setting for the performance parameters instance.
     */
    @XmlElement(name = "dedup_capable", required = false)
    public Boolean getDedupCapable() {
        return dedupCapable;
    }

    public void setDedupCapable(Boolean dedupCapable) {
        this.dedupCapable = dedupCapable;
    }

    /**
     * The fast expansion setting for the performance parameters instance.
     */
    @XmlElement(name = "fast_expansion", required = false)
    public Boolean getFastExpansion() {
        return fastExpansion;
    }

    public void setFastExpansion(Boolean fastExpansion) {
        this.fastExpansion = fastExpansion;
    }

    /*
     * Utility Methods
     */

    @JsonIgnore
    public boolean isHostIOLimitBandwidthSet() {
        return hostIOLimitBandwidth != null && hostIOLimitBandwidth > 0;
    }

    @JsonIgnore
    public boolean isHostIOLimitIOPsSet() {
        return hostIOLimitIOPs != null && hostIOLimitIOPs > 0;
    }
}