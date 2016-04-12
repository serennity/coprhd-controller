/*
 * Copyright (c) 2008-2013 EMC Corporation
 * All Rights Reserved
 */
package com.emc.storageos.plugins.common;

import java.util.regex.Pattern;

import javax.cim.CIMObjectPath;
import javax.cim.UnsignedInteger32;

public class Constants {
    public static final String _computerSystem = "ComputerSystem";
    public static final String _cimClient = "cimClient";
    public static final String _cimSystem = "CIM_ComputerSystem";
    public static final String _serialID = "serialID";
    public static final String _softwareVersion = "softwareVer";
    public static final String dbClient = "dbClient";
    public static final String COORDINATOR_CLIENT = "coordinator";
    public static final String networkDeviceController = "networkDeviceController";
    public static final String _Volumes = "Volumes";
    public static final String _Volume = "Volume";
    public static final String _SystemName = "SystemName";
    public static final String _DeviceID = "DeviceID";
    public static final String _Stats = "Stats";
    public static final String _plusDelimiter = "+";
    public static final String _InteropNamespace = "InteropNamespace";
    public static final String SV_BLOCK_SIZE = "SVBlockSize";
    public static final String SV_NUMBER_BLOCKS = "SVConsumableBlocks";
    public static final String BLOCK_SIZE = "BlockSize";
    public static final String NUMBER_OF_BLOCKS = "ConsumableBlocks";
    public static final String _SystemElement = "SystemElement";
    public static final String _SyncedElement = "SyncedElement";
    public static final String _SyncType = "SyncType";
    public static final String _Seven = "7";
    public static final String _Name = "Name";
    public static final String _enum = "enum";
    public static final String _CreationClassName = "CreationClassName";
    public static final String _StorageSystem = "StorageSystem";
    public static final String _Block = "block";
    public static final String _RP = "rp";
    public static final String _File = "file";
    public static final String _Object = "object";
    public static final String _TimeLastSampled = "TimeLastSampled";
    public static final String _TimeCollected = "TimeCollected";
    public static final String _TimeLastMeasured = "TimeLastMeasured";
    public static final String _FilePreCompressionBytesWritten = "FilePreCompBytesWritten";
    public static final String _FilePostCompressionBytesWritten = "FilePostCompBytesWritten";
    public static final String _CompressionRatio = "CompressionRatio";
    public static final String _Granularity = "Granularity";
    public static final String _minusDelimiter = "-";
    public static final String _datepattern = "yyyyMMddHHmmss.SSSSSSZ";
    public static final String _debug = "debug";
    public static final String _cache = "Cache";
    public static final String _cachedData = "CachedData";
    public static final String _nativeGUIDs = "nativeGUIDs";
    public static final String _globalCacheKey = "globalCacheKey";
    public static final String _cachePools = "cachepools";
    public static final String _cassandraInsertion = "cassandraInsertion";
    public static final char STAT_ID_SEPARATOR = '-';
    public static final String _storagePool = "storagePool";
    public static final String STORAGEPOOLS = "storagePools";
    public static final String _cimPool = "CIM_StoragePool";
    public static final String _rpClient = "rpClient";
    public static final String _rpSystem = "RP_ProtectionSystem";
    public static final String EmcStorageSystem = "EMC_StorageSystem";
    public static final String ACCESSPROFILE = "accessProfile";
    public static final String DETECTED = "detected";
    public static final String REGISTEREDPROFILE = "profile";
    public static final String PROFILECLASS = "CIM_RegisteredProfile";
    public static final String STORAGEPROCESSORS = "storageProcessors";
    public static final String STORAGEPORTS = "storagePorts";
    public static final String POOLCAPABILITIES = "poolCapabilities";
    public static final String PHYSICAL = "physical";
    public static final String LOGICAL = "logical";
    public static final String SYSTEMCACHE = "systemCache";
    public static final String STORAGEETHERNETPORTS = "storagePorts";
    public static final String INTEROP = "interop";
    public static final String EMC_NAMESPACE = "root/emc";
    public static final String IPENDPOINTS = "ipEndPoints";
    public static final String PROPS = "props";
    public static final String METERINGDUMP = "metering-dump";
    public static final String METERINGDUMPLOCATION = "metering-dump-location";
    public static final String DISCOVEREDARRAYS = "arraysList";
    public static final String CAPABILITIES = "+capabilities";
    public static final String NULL_STR = "null";
    public static final String DEVICEANDTHINPOOLS = "devicePools";
    public static final String THINPOOLS = "thinPools";
    public static final String PROVIDER_VERSION = "controller_smis_provider_version";
    public static final String PROTOCOLS = "protocols";
    public static final String VMAXFASTPOLICIES = "vmaxFASTPolicies";
    public static final String VNXFASTPOLICIES = "vnxFASTPolicies";
    public static final String STORAGETIERS = "storageTiers";
    public static final String STORAGEVOLUMES = "storagevolumes";
    public static final String FASTPOLICY = "policyRule";
    public static final String CIMFASTPOLICYRULE = "CIM_TierPolicyRule";
    public static final String POLICY_TO_POOLS_MAPPING = "policyToPoolMapping";
    public static final String VMAXConfigurationService = "vmaxConfigurationService";
    public static final String VNXConfigurationService = "vnxConfigurationService";
    public static final String VMAXTierPolicyService = "vmaxTierPolicyService";
    public static final String VNXTierPolicyService = "vnxTierPolicyService";
    public static final String CONFIGURATIONSERVICE = "configurationService";
    public static final String EMCCONTROLLERCONFIGURATIONSERVICE = "EMC_ControllerConfigurationService";
    public static final String TIERPOLICYSERVICE = "tierPolicyService";
    public static final String EMCTIERPOLICYSERVICE = "EMC_TierPolicyService";
    public static final String REPLICATIONSERVICE = "replicationService";
    public static final String DEVICEMASKINGROUPS = "deviceMaskingGroups";
    public static final String SYSTEMCREATEDDEVICEGROUPNAMES = "systemCreatedDeviceGroupNames";
    public static final String USED_IN_CHECKING_GROUPNAMES_TO_FASTPOLICY = "deviceGroupNamesToPolicyExistence";
    public static final String USED_IN_CHECKING_GROUPNAMES_EXISTENCE = "deviceGroupNamesExistence";
    public static final String SYMMETRIX = "symmetrix";
    public static final String CLARIION = "clariion";
    public static final String IBMXIV_PROVIDER_VERSION = "controller_ibmxiv_provider_version";
    public static final String IBMXIV_CLASS_PREFIX = "IBMTSDS_";
    public static final String IBM_NAMESPACE = "root/ibm";
    public static final String IBM_STORAGE_SYSTEM = "IBMTSDS_StorageSystem";
    public static final String XIV = "xiv";
    public static final String USED_IN_CHECKING_THICK_GROUPNAMES_EXISTENCE = "thickdeviceGroupNamesExistence";
    public static final String USED_IN_CHECKING_THIN_GROUPNAMES_EXISTENCE = "thindeviceGroupNamesExistence";
    public static final String USED_IN_CHECKING_ALL_GROUPNAMES_EXISTENCE = "thinandthickdeviceGroupNamesExistence";
    public static final String USED_IN_CHECKING_THICKGROUPNAMES_TO_FASTPOLICY = "thickdeviceGroupNamesToPolicyExistence";
    public static final String USED_IN_CHECKING_THINGROUPNAMES_TO_FASTPOLICY = "thindeviceGroupNamesToPolicyExistence";
    public static final String USED_IN_CHECKING_ALLGROUPNAMES_TO_FASTPOLICY = "alldeviceGroupNamesToPolicyExistence";
    public static final String ARRAYTYPE = "arrayType";
    public static final String VNXPOOLS = "vnxpools";
    public static final String VNXPOOLCAPABILITIES = "vnxpoolcapabilities";
    public static final String VNXPOOLSETTINGS = "vnxpoolsettings";
    public static final String VNXPOOLSETTINGINSTANCES = "vnxpoolsettinginstances";
    public static final String VNXPOOLCAPABILITIES_TIER = "vnxpoolcapabilities_tier";
    public static final String HYPHEN = "-";
    public static final String PLUS = "+";
    public static final String SYMMETRIX_U = "SYMMETRIX";
    public static final String INSTANCEID = "InstanceID";
    public static final String ELEMENTTYPE = "ElementType";
    public static final String VOLUME_ELEMENTTYPE = "8";
    public static final String SYSTEM_ELEMENTTYPE = "2";
    public static final String FEPORT_ELEMENTTYPE = "6";
    public static final String CSV_SEQUENCE = "CSVSequence";
    public static final String POLICYRULENAME = "PolicyRuleName";
    public static final String EMC_FAST_SETTING = "EMCFastSetting";
    public static final String EMC_SLO = "EMCSLO";
    public static final String EMC_WORKLOAD = "EMCWorkload";
    public static final String EMC_AVG_RESPONSE_TIME = "EMCApproxAverageResponseTime";
    public static final String SYSTEMNAME = "SystemName";
    public static final String SE_DEVICEMASKINGGROUP = "SE_DeviceMaskingGroup";
    public static final String TIERMETHODOLOGY = "tiermethodology";
    public static final String INITIAL_STORAGE_TIER_METHODOLOGY = "InitialStorageTierMethodology";
    public static final String SUPPORTED_COPY_TYPES = "SupportedCopyTypes";
    public static final String SUPPORTED_ASYNCHRONOUS_ACTIONS = "SupportedAsynchronousActions";
    public static final String THIN_PROVISIONED_CLIENT_SETTABLE_RESERVE = "ThinProvisionedClientSettableReserve";
    public static final String RULEDISCRIMINATOR = "RuleDiscriminator";
    public static final String LOCALRULE = "SNIA:LocalRule";
    public static final String DELIMITER = "-";
    public static final String THICKDEVICEGROUP = "thickDeviceGroup";
    public static final String THINDEVICEGROUP = "thinDeviceGroup";
    public static final String THINANDTHICKDEVICEGROUP = "thinandthickDeviceGroup";
    public static final String DEVICEGROUP = "devicegroup";
    public static final String THINLUN = "thinlun";
    public static final String ENABLED = "Enabled";
    public static final String PROVISIONING_TYPE = "ProvisioningType";
    public static final String DEVICEID = "DeviceID";
    public static final int NO_DATA_MOVEMENT = 2;
    public static final int AUTO_TIER = 3;
    public static final int HIGH_AVAILABLE_TIER = 6;
    public static final int LOW_AVAILABLE_TIER = 7;
    public static final int START_HIGH_THEN_AUTO_TIER = 4;
    public static final String START_HIGH_THEN_AUTO_TIER_POLICY_NAME = "DEFAULT_START_HIGH_THEN_AUTOTIER";
    public static final int DEFAULT_PARTITION_SIZE = 100;
    public static final String METERING_RECORDS_PARTITION_SIZE = "metering-records-partition-size";
    public static final String POOLSETTINGS = "poolsettings";
    public static final String MODIFIED_SETTING_INSTANCES = "modifiedSettingInstances";
    public static final String MODIFIED_STORAGEPOOLS = "modified_storagepools";
    public static final String POOL_MATCHER = "PoolMatcher";
    public static final long DEFAULT_LOCK_ACQUIRE_TIME = 2 * 60;
    public static final long DISCOVER_LOCK_ACQUIRE_TIME = 2 * 60;
    public static final long SCAN_LOCK_ACQUIRE_TIME = 5 * 60;
    public static final long METERING_LOCK_ACQUIRE_TIME = 0;
    public static final String SYSTEMID = "systemId";
    public static final String TIERDOMAIN = "tierDomain";
    public static final String ELEMENTNAME = "ElementName";
    public static final String TECHNOLOGY = "Technology";
    public static final String PERCENTAGE = "Percentage";
    public static final String DRIVE_TYPE = "driveType";
    public static final String NAME = "Name";
    public static final String TIERDOMAINS = "tierDomains";
    public static final String VMAXPOOLS = "vmaxpools";
    public static final String VMAX2POOLS = "vmax2pools";
    public static final String VMAX2_THIN_POOLS = "vmax2ThinPools";
    public static final String VMAX2_THIN_POOL_TO_BOUND_VOLUMES = "vmax2ThinPoolToBoundVolumes";
    public static final String TIER = "tier";
    public static final String EMC_STORAGE_TIER = "EMC_StorageTier";
    public static final String MANIFEST_COLLECTION_NAME = "StorageOS_Metrics";
    public static final String MANIFEST_EXISTS = "manifestExists";
    public static final String VOLUME_SEQUENCE = "VolumeSequence";
    public static final String SYSTEM_SEQUENCE = "SystemSequence";
    public static final String FEPORT_SEQUENCE = "FeportSequence";
    public static final String STORAGEOS_VOLUME_MANIFEST = "StorageOS_Volumes";
    public static final String STORAGEOS_SYSTEM_MANIFEST = "StorageOS_System";
    public static final String STORAGEOS_FEPORT_MANIFEST = "StorageOS_FEPort";
    public static final String SEMI_COLON = ";";
    public static final String COLON = ":";
    public static final int ASYNC_COPY_TYPE = 2;
    public static final int SYNC_COPY_TYPE = 3;
    public static final int UNSYNC_ASSOC_COPY_TYPE = 4;
    public static final int UNSYNC_UNASSOC_COPY_TYPE = 5;
    public static final int CREATE_ELEMENT_REPLICA_ASYNC_ACTION = 2;
    public static final int CREATE_GROUP_REPLICA_ASYNC_ACTION = 3;
    public static final String VOLUME_STORAGE_GROUP_MAPPING = "volumesToStorageGroupMapping";
    public static final String MASKING_GROUPS = "maskingGroups";
    public static final String STORAGEID = "StorageID";
    public static final String STORAGE_VOLUME_VIEWS = "storagevolumeviews";
    public static final String MASKING_VIEWS = "maskingViews";
    public static final String EXPORTED_VOLUMES = "exportedVolumes";
    public static final String VOLUMES_WITH_SLOS = "volumesWithSLO";
    public static final String VOLUME_SPACE_CONSUMED_MAP = "volumeToSpaceConsumed";
    public static final String TOTAL_CAPACITY = "TotalCapacity";
    public static final String INITIAL_STORAGE_TIERING_SELECTION = "InitialStorageTieringSelection";
    public static final int RELATIVE_PERFORMANCE_ORDER = 2;
    public static final String NONE = "None";
    public static final String OPTIMIZED_SLO = "Optimized";
    public static final String VOLUMES_PART_OF_CG = "volumesPartOfCG";
    public static final String REPLICATIONGROUPS = "replicationGroups";
    public static final String META_VOLUMES_VIEWS = "metaVolumesViewList";
    public static final String META_VOLUMES = "metaVolumesList";
    /* NTAP metrics to be recorded */
    /* TODO: Block size should be read from the array, if possible. */
    public static final int NETAPP_BYTES_PER_BLOCK = 1024;
    public static final String SIZE_TOTAL = "size-total";
    public static final String OWNING_VFILER = "owning-vfiler";
    public static final String STORAGE_GROUPS = "storageGroups";
    public static final String AUTO_TIER_VOLUMES = "autoTierVolumes";
    public static final String SIZE_USED = "size-used";
    public static final String SNAPSHOT_BLOCKS_RESERVED = "snapshot-blocks-reserved";
    public static final String SNAPSHOT_BYTES_RESERVED = "snapshot-bytes-reserved";
    public static final String SNAPSHOT_COUNT = "snapshot-count";
    public static final String PARENT = "parent";
    public static final String STORAGE_DEVICE = "storageDevice";
    public static final String INITIATOR_HLU_MAP = "initiatirToHLU";
    public static final String SYSTEMS_RUN_RP_CONNECTIVITY = "systemsToRunRPConnectivity";
    public static final String STORAGE_PORTS = "portsToRunNetworkConn";
    public static final String SUPPORTED_REPLICATION_TYPES = "SupportedReplicationTypes";
    public static final String ENDPOINTS_RAGROUP = "endPointsToRAGroup";
    public static final String VOLUME_RAGROUP = "volumeToRAGroup";
    public static final String RAGROUP = "RAGroup";
    public static final String REMOTE_MIRRORING = "remoteMirroring";
    public static final String UN_VOLUMES_RAGP = "unManagedVolumesInRAGroup";
    public static final String UN_VOLUME_RAGROUP_MAP = "unManagedVolumesToRAGroupMap";
    public static final String UN_VOLUME_LOCAL_REPLICA_MAP = "unManagedVolumesToLocalReplicaMap";
    public static final String SNAPSHOT_NAMES_SYNCHRONIZATION_ASPECT_MAP = "snapshotsToSynchronizationAspects";
    public static final String DUPLICATE_SYNC_ASPECT_ELEMENT_NAME_MAP = "duplicateSyncAspectElementNameMap";
    public static final String NOT_INGESTABLE_SYNC_ASPECT = "notIngestableSyncAspect";
    public static final String UNMANAGED_EXPORT_MASKS_MAP = "unManagedExportMasksMap";
    public static final String UNMANAGED_EXPORT_MASKS_SET = "unManagedExportMasksSet";
    public static final String UNMANAGED_EXPORT_MASKS_CREATE_LIST = "unManagedExportMasksCreateList";
    public static final String UNMANAGED_EXPORT_MASKS_UPDATE_LIST = "unManagedExportMasksUpdateList";
    public static final String UNMANAGED_EXPORT_MASKS_VPLEX_INITS_SET = "unManagedExportMasksVplexInitsSet";
    public static final String UNMANAGED_EXPORT_MASKS_RECOVERPOINT_INITS_SET = "unManagedExportMasksRecoverPointInitsSet";
    public static final String UNMANAGED_VPLEX_BACKEND_MASKS_SET = "unManagedVplexBackendVolumesSet";
    public static final String UNMANAGED_RECOVERPOINT_MASKS_SET = "unManagedRecoverPointVolumesSet";
    public static final String REMOTE_COPY_MODE = "remoteCopyMode";
    public static final String COPY_STATE = "EMCCopyState";
    public static final String COPY_STATE_DESC = "EMCCopyStateDesc";
    public static final String SRDF_LINKS = "srdfLinks";
    public static final String SOFTWARE_IDENTITY = "softwareIdentity";
    public static final String EMC_SOFTWARE_IDENTITY = "EMC_StorageSystemSoftwareIdentity";
    public static final String POLICY_STORAGE_GROUP_MAPPING = "policyToGroup";
    public static final String ACCESS = "Access";
    public static final String EVENT_MANAGER = "EventManager";
    public static final String STORAGE_SYNCHRONIZED_SV_SV = "SE_StorageSynchronized_SV_SV";
    public static final String DISCOVERED_PORTS = "discoveredPorts";
    public static final CIMObjectPath SYNC_PATH = new CIMObjectPath(null, null, null, Constants.EMC_NAMESPACE,
            Constants.STORAGE_SYNCHRONIZED_SV_SV, null);
    private static final String EMC_LUNMASKING_PROTOCOL_CONTROLLER = "EMC_LunMaskingSCSIProtocolController";

    public static final CIMObjectPath MASKING_PATH = new CIMObjectPath(null, null, null, Constants.EMC_NAMESPACE,
            Constants.EMC_LUNMASKING_PROTOCOL_CONTROLLER, null);
    public static final UnsignedInteger32 SYNC_BATCH_SIZE = new UnsignedInteger32(200);
    public static final String STORAGE_VOLUME = "CIM_StorageVolume";

    /* Compute System Related Constants */
    public static final String COMPUTE = "compute";
    public static final String USING_SMIS80_DELIMITERS = "Using SMI-S 8.0 Delimiters";
    public static final String SMIS80_DELIMITER = "-+-";
    public static final String SMIS80_DELIMITER_REGEX = "-\\+-";
    public static final String SMIS_PLUS_REGEX = "\\+";
    public static final String SMIS_DOT_REGEX = "\\.";
    public static final String PATH_DELIMITER_REGEX = "-\\+-|\\+";
    public static final Pattern PATH_DELIMITER_PATTERN = Pattern.compile(PATH_DELIMITER_REGEX);
    public static final String UNDERSCORE_DELIMITER = "_";
    public static final String STORAGE_GROUP_PREFIX = "ViPR_";
    public static final int STOARGE_GROUP_MAX_LENGTH = 64;
    public static final int VMAX3_FULLY_ALLOCATED_VOLUME_PERCENTAGE = 100;
    public static final String FEADAPT_ELEMENTTYPE = "3";
    public static final String STORAGEOS_FEADAPT_MANIFEST = "StorageOS_FEAdapt";
    public static final String CLOCK_TICK_INTERVAL = "ClockTickInterval";
    public static final String SMIS_80_STYLE = "\\-\\+\\-";

    public static final String VERSION = "VERSION";
    public static final String IS_NEW_SMIS_PROVIDER = "isNewSMIS";
    public static final String STORAGE_GROUPS_PROCESSED = "StorageGroupsProccessed";

    public static final String WORKLOAD = "Workload";
    public static final String SLO_NAMES = "SLONames";
    public static final String EXTERNALDEVICE = "externaldevice";

}
