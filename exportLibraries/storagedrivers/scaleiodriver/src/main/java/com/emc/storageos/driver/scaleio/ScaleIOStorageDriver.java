/*
 * Copyright 2016 Oregon State University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.emc.storageos.driver.scaleio;

import com.emc.storageos.driver.scaleio.api.ScaleIOConstants;
import com.emc.storageos.driver.scaleio.api.restapi.ScaleIORestClient;
import com.emc.storageos.driver.scaleio.api.restapi.response.*;
import com.emc.storageos.storagedriver.AbstractStorageDriver;
import com.emc.storageos.storagedriver.BlockStorageDriver;
import com.emc.storageos.storagedriver.DriverTask;
import com.emc.storageos.storagedriver.RegistrationData;
import com.emc.storageos.storagedriver.model.*;
import com.emc.storageos.storagedriver.storagecapabilities.CapabilityInstance;
import com.emc.storageos.storagedriver.storagecapabilities.StorageCapabilities;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.apache.commons.lang.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.*;

public class ScaleIOStorageDriver extends AbstractStorageDriver implements BlockStorageDriver {
	private static final Logger log = LoggerFactory.getLogger(ScaleIOStorageDriver.class);
	private String fullyQualifiedXMLConfigName = "/scaleio-driver-prov.xml";
	private ApplicationContext context = new ClassPathXmlApplicationContext(fullyQualifiedXMLConfigName);
	private ScaleIORestHandleFactory scaleIORestHandleFactory = (ScaleIORestHandleFactory) context.getBean("scaleIORestHandleFactory");
	private ScaleIORestClient client;
	private int countSucc;

	/**
	 * Create storage volumes with a given set of capabilities.
	 * Before completion of the request, set all required data for provisioned volumes in "volumes" parameter.
	 *
	 * @param volumes      Input/output argument for volumes.
	 * @param capabilities Input argument for capabilities. Defines storage capabilities of volumes to create.
	 * @return task
	 */
	@Override
	public DriverTask createVolumes(List<StorageVolume> volumes, StorageCapabilities capabilities) {
		DriverTask task = new DriverTaskImpl(ScaleIOHelper.getTaskId(ScaleIOConstants.TaskType.VOLUME_CREATE));

		if (volumes != null && !volumes.isEmpty()) {
			int successful = 0;

			// Assume volumes can be created for different storage systems
			for (StorageVolume volume : volumes) {
				String capacity = volume.getRequestedCapacity().toString();

				ScaleIORestClient restClient = getClientBySystemId(volume.getStorageSystemId());

				if (restClient != null) {
					ScaleIOVolume result;

					try {

						result = restClient.addVolume(volume.getStorageSystemId(), volume.getStoragePoolId(),
								volume.getDisplayName(), capacity);

						if (result != null) {
							volume.setNativeId(result.getId());
							long sizeInBytes = Long.parseLong(result.getSizeInKb()) * 1024;
							volume.setAllocatedCapacity(sizeInBytes);

							successful++;
						} else {
							log.error("Error while creating volume " + volume.getDisplayName() +
									" in system " + volume.getStorageSystemId());
						}

					} catch (Exception e) {
						log.error("Error while processing volume " + volume.getDisplayName(), e);

					}

				} else {
					log.error("Could not get client for storage system: " + volume.getStorageSystemId());

				}
			}

			setTaskStatus(volumes.size(), successful, task);
			task.setMessage("Task succeeded for " + successful + " of " + volumes.size() + " volumes");

		} else {
			log.error("Empty volume input list");
			task.setMessage("Task failed because volume list was empty");
			task.setStatus(DriverTask.TaskStatus.FAILED);
		}

		return task;
	}

	/**
	 * Expand volume.
	 * Before completion of the request, set all required data for expanded volume in "volume" parameter.
	 *
	 * @param volume      Volume to expand. Type: Input/Output argument.
	 * @param newCapacity Requested capacity in GB. Type: input argument.
	 * @return task
	 */
	@Override
	public DriverTask expandVolume(StorageVolume volume, long newCapacity) {
		DriverTask task = new DriverTaskImpl(ScaleIOHelper.getTaskId(ScaleIOConstants.TaskType.VOLUME_EXPAND));

		if (newCapacity > 0) {
			ScaleIORestClient restClient = getClientBySystemId(volume.getStorageSystemId());

			if (restClient != null) {
				ScaleIOVolume result;

				try {

					result = restClient.modifyVolumeCapacity(volume.getNativeId(), String.valueOf(newCapacity));

					if (result != null) {
						task.setStatus(DriverTask.TaskStatus.READY);
						task.setMessage("Volume " + volume.getNativeId() + " expanded to " + newCapacity + " GB");
						return task;
					} else {
						log.error("Error while expanding volume" + volume.getDisplayName());
					}

				} catch (Exception e) {
					log.error("Error while processing volume" + volume.getDisplayName(), e);
				}

			} else {
				log.error("Could not get client for storage system: " + volume.getStorageSystemId());

			}

		} else {
			log.error("Invalid new capacity: " + newCapacity);
		}

		task.setStatus(DriverTask.TaskStatus.FAILED);
		task.setMessage("Expand operation failed for volume " + volume.getNativeId());
		return task;
	}

	/**
	 * Delete volumes.
	 *
	 * @param volumes Volumes to delete.
	 * @return task
	 */
	@Override
	public DriverTask deleteVolumes(List<StorageVolume> volumes) {
		DriverTask task = new DriverTaskImpl(ScaleIOHelper.getTaskId(ScaleIOConstants.TaskType.VOLUME_DELETE));

		if (!volumes.isEmpty()) {
			int successful = 0;

			for (StorageVolume volume : volumes) {
				ScaleIORestClient restClient = getClientBySystemId(volume.getStorageSystemId());

				if (restClient != null) {
					try {
						restClient.removeVolume(volume.getNativeId());
						successful++;

					} catch (Exception e) {
						log.error("Error while deleting volume " + volume.getNativeId() +
								"in storage system " + volume.getStorageSystemId(), e);
					}

				} else {
					log.error("Could not get client for storage system: " + volume.getStorageSystemId());

				}
			}

			setTaskStatus(volumes.size(), successful, task);
			task.setMessage("Task succeeded for " + successful + " of " + volumes.size() + " volumes");

		} else {
			task.setStatus(DriverTask.TaskStatus.FAILED);
			task.setMessage("Task failed because volume list was empty");
			log.error("Empty volume input list");
		}

		return task;
	}

	/**
	 * Create volume snapshots.
	 *
	 * @param snapshots    Type: Input/Output.
	 * @param capabilities capabilities required from snapshots. Type: Input.
	 * @return task
	 */
	@Override
	public DriverTask createVolumeSnapshot(List<VolumeSnapshot> snapshots, StorageCapabilities capabilities) {
		log.info("Request to create Snapshots -- Start :");
		DriverTask task = new DriverTaskImpl(ScaleIOHelper.getTaskId(ScaleIOConstants.TaskType.SNAPSHOT_CREATE));
		countSucc = 0;
		// Assume snapshots could be from different storage system
		if (snapshots != null && snapshots.size() > 0) {
			// Assume snapshots could be from different storage system
			for (VolumeSnapshot snapshot : snapshots) {
				log.info("Start to get Rest client for volume {} of ScaleIO storage system: {}", snapshot.getParentId(),
						snapshot.getStorageSystemId());
				client = getClientBySystemId(snapshot.getStorageSystemId());
				// create snapshot
				if (client != null) {
					ScaleIOSnapshotVolumeResponse result = null;
					try {
						log.info("Client got! Create snapshot for volume {}:{} - start", snapshot.getDisplayName(), snapshot.getParentId());
						result = client.snapshotVolume(snapshot.getParentId(), snapshot.getDisplayName(),
								snapshot.getStorageSystemId());
						// set value to the output
						if (result != null) {
							snapshot.setNativeId(result.getVolumeIdList().get(0));
							snapshot.setTimestamp(ScaleIOHelper.getCurrentTime());
							snapshot.setAccessStatus(StorageObject.AccessStatus.READ_WRITE);
							Map<String, String> snapNameIdMap = client.getVolumes(result.getVolumeIdList());
							snapshot.setDeviceLabel(snapNameIdMap.get(snapshot.getNativeId()));
							countSucc++;
							log.info("Successfully create snapshot for volume {}:{} - end", snapshot.getDisplayName(),
									snapshot.getParentId());
						} else {
							log.info("No snapshot returned for volume {}:{} ", snapshot.getDisplayName(), snapshot.getParentId());
						}
					} catch (Exception e) {
						log.error("Exception while creating snapshot for volume {}", snapshot.getParentId(), e);
					}
				} else {
					log.error("Exception while getting client instance for volume {}:{}", snapshot.getDisplayName(), snapshot.getParentId());
				}
			}
			setTaskStatus(snapshots.size(), countSucc, task);
		} else {
			log.error("Empty snapshot input List");
			task.setStatus(DriverTask.TaskStatus.FAILED);
		}
		task.setEndTime(Calendar.getInstance());
		log.info("Request to create Snapshots -- End ");
		return task;
	}

	/**
	 * Restore volume to snapshot state.
	 *
	 * @param volume   Type: Input/Output.
	 * @param snapshot Type: Input.
	 * @return task
	 */
	@Override
	public DriverTask restoreSnapshot(StorageVolume volume, VolumeSnapshot snapshot) {
		return setUpNonSupportedTask(ScaleIOConstants.TaskType.SNAPSHOT_RESTORE);
	}

	/**
	 * Delete snapshots.
	 *
	 * @param snapshots Type: Input.
	 * @return task
	 */
	@Override
	public DriverTask deleteVolumeSnapshot(List<VolumeSnapshot> snapshots) {
		log.info("Request to delete Snapshots -- Start :");
		DriverTask task = new DriverTaskImpl(ScaleIOHelper.getTaskId(ScaleIOConstants.TaskType.SNAPSHOT_DELETE));
		countSucc = 0;
		// Assume snapshots could be from different storage system
		if (snapshots != null && snapshots.size() > 0) {
			for (VolumeSnapshot snapshot : snapshots) {
				log.info("Get Rest client for snapshot {}:{} - start", snapshot.getDisplayName(), snapshot.getNativeId());
				client = getClientBySystemId(snapshot.getStorageSystemId());
				// delete snapshot
				if (client != null) {
					try {
						log.info("Rest client Got! delete snapshot {}:{} - start", snapshot.getDisplayName(), snapshot.getNativeId());
						client.removeVolume(snapshot.getNativeId());
						countSucc++;
						log.info("Successfully delete snapshot {}:{} - end", snapshot.getDisplayName(), snapshot.getNativeId());
					} catch (Exception e) {
						log.error("Exception while deleting snapshot {}", snapshot.getNativeId(), e);
					}
				} else {
					log.error("Exception while getting client instance for snapshot {}:{}", snapshot.getDisplayName(),
							snapshot.getNativeId());
				}
			}
			setTaskStatus(snapshots.size(), countSucc, task);
		} else {
			log.error("Can't delete empty snapshot list");
			task.setStatus(DriverTask.TaskStatus.FAILED);
		}
		task.setEndTime(Calendar.getInstance());
		log.info("Request to delete Snapshots -- End ");
		return task;
	}

	/**
	 * Clone volume clones.
	 *
	 * @param clones       Type: Input/Output.
	 * @param capabilities capabilities of clones. Type: Input.
	 * @return task
	 */
	@Override
	public DriverTask createVolumeClone(List<VolumeClone> clones, StorageCapabilities capabilities) {
		return null;
	}

	/**
	 * Detach volume clones.
	 *
	 * @param clones Type: Input/Output.
	 * @return task
	 */
	@Override
	public DriverTask detachVolumeClone(List<VolumeClone> clones) {
		return null;
	}

	@Override
	public DriverTask restoreFromClone(List<VolumeClone> clones) {
		return null;
	}

	/**
	 * Delete volume clones.
	 *
	 * @param clones clones to delete. Type: Input.
	 * @return task
	 */
	@Override
	public DriverTask deleteVolumeClone(List<VolumeClone> clones) {
		return null;
	}

	/**
	 * Create volume mirrors.
	 *
	 * @param mirrors      Type: Input/Output.
	 * @param capabilities capabilities of mirrors. Type: Input.
	 * @return task
	 */
	@Override
	public DriverTask createVolumeMirror(List<VolumeMirror> mirrors, StorageCapabilities capabilities) {
		return setUpNonSupportedTask(ScaleIOConstants.TaskType.MIRROR_OPERATIONS);
	}

	/**
	 * Delete mirrors.
	 *
	 * @param mirrors mirrors to delete. Type: Input.
	 * @return task
	 */
	@Override
	public DriverTask deleteVolumeMirror(List<VolumeMirror> mirrors) {
		return setUpNonSupportedTask(ScaleIOConstants.TaskType.MIRROR_OPERATIONS);
	}

	/**
	 * Split mirrors
	 *
	 * @param mirrors Type: Input/Output.
	 * @return task
	 */
	@Override
	public DriverTask splitVolumeMirror(List<VolumeMirror> mirrors) {
		return setUpNonSupportedTask(ScaleIOConstants.TaskType.MIRROR_OPERATIONS);
	}

	/**
	 * Resume mirrors after split
	 *
	 * @param mirrors Type: Input/Output.
	 * @return task
	 */
	@Override
	public DriverTask resumeVolumeMirror(List<VolumeMirror> mirrors) {
		return setUpNonSupportedTask(ScaleIOConstants.TaskType.MIRROR_OPERATIONS);
	}

	/**
	 * Restore volume from a mirror
	 *
	 * @param volume Type: Input/Output.
	 * @param mirror Type: Input.
	 * @return task
	 */
	@Override
	public DriverTask restoreVolumeMirror(StorageVolume volume, VolumeMirror mirror) {
		return setUpNonSupportedTask(ScaleIOConstants.TaskType.MIRROR_OPERATIONS);
	}

	/**
	 * Get export masks for a given set of initiators.
	 *
	 * @param storageSystem Storage system to get ITLs from. Type: Input.
	 * @param initiators    Type: Input.
	 * @return list of export masks
	 */
	@Override
	public List<ITL> getITL(StorageSystem storageSystem, List<Initiator> initiators) {
		return null;
	}

	@Override
	public DriverTask exportVolumesToInitiators(List<Initiator> initiators, List<StorageVolume> volumes, Map<String, String> volumeToHLUMap, List<StoragePort> recommendedPorts, List<StoragePort> availablePorts, StorageCapabilities capabilities, MutableBoolean usedRecommendedPorts, List<StoragePort> selectedPorts) {
		return null;
	}


	/**
	 * Export volumes to initiators through a given set of ports. If ports are not provided,
	 * use port requirements from ExportPathsServiceOption storage capability
	 *
	 * @param initiators       Type: Input.
	 * @param volumes          Type: Input.
	 * @param recommendedPorts list of storage ports recommended for the export. Optional. Type: Input.
	 * @param capabilities     storage capabilities. Type: Input.
	 * @return task
	 */
	public DriverTask exportVolumesToInitiators(List<Initiator> initiators, List<StorageVolume> volumes, List<StoragePort> recommendedPorts,
	                                            StorageCapabilities capabilities) {
		return null;
	}

	/**
	 * Unexport volumes from initiators
	 *
	 * @param initiators Type: Input.
	 * @param volumes    Type: Input.
	 * @return task
	 */
	@Override
	public DriverTask unexportVolumesFromInitiators(List<Initiator> initiators, List<StorageVolume> volumes) {
		return null;
	}

	/**
	 * Create block consistency group.
	 *
	 * @param consistencyGroup input/output
	 * @return task
	 */
	@Override
	public DriverTask createConsistencyGroup(VolumeConsistencyGroup consistencyGroup) {
		return setUpNonSupportedTask(ScaleIOConstants.TaskType.CG_CREATE);
	}

	/**
	 * Delete block consistency group.
	 *
	 * @param consistencyGroup Input
	 * @return task
	 */
	@Override
	public DriverTask deleteConsistencyGroup(VolumeConsistencyGroup consistencyGroup) {
		return setUpNonSupportedTask(ScaleIOConstants.TaskType.CG_DELETE);
	}

	/**
	 * Create snapshot of consistency group.
	 *
	 * @param consistencyGroup input parameter
	 * @param snapshots        input/output parameter
	 * @param capabilities     Capabilities of snapshots. Type: Input.
	 * @return task
	 */
	@Override
	public DriverTask createConsistencyGroupSnapshot(VolumeConsistencyGroup consistencyGroup, List<VolumeSnapshot> snapshots,
	                                                 List<CapabilityInstance> capabilities) {
		log.info("Request to create consistency group snapshot -- Start :");
		DriverTask task = new DriverTaskImpl(ScaleIOHelper.getTaskId(ScaleIOConstants.TaskType.CG_SNAP_CREATE));
		countSucc = 0;
		if (ScaleIOHelper.isFromSameStorageSystem(snapshots)) {
			String systemId = snapshots.get(0).getStorageSystemId();
			log.info("Start to get Rest client for ScaleIO storage system: {}", systemId);
			ScaleIORestClient client = getClientBySystemId(systemId);
			if (client != null) {
				try {
					log.info("Rest Client Got! Create consistency group snapshot - Start:");
					Map<String, String> parent2snap = new HashMap<>();
					for (VolumeSnapshot snapshot : snapshots) {
						parent2snap.put(snapshot.getParentId(), snapshot.getDisplayName());
					}
					ScaleIOSnapshotVolumeResponse result = client.snapshotMultiVolume(parent2snap, systemId);

					// set value to the output
					if (consistencyGroup == null) {
						consistencyGroup = new VolumeConsistencyGroup();
					}
					consistencyGroup.setNativeId(result.getSnapshotGroupId());
					consistencyGroup.setStorageSystemId(systemId);

					// get parentID
					List<String> nativeIds = result.getVolumeIdList();
					Map<String, ScaleIOVolume> snapIdInfoMap = client.getVolumeNameMap(nativeIds);
					String currentTime = ScaleIOHelper.getCurrentTime();
					for (VolumeSnapshot snapshot : snapshots) {
						for (ScaleIOVolume snapInfo : snapIdInfoMap.values()) {
							if (snapshot.getParentId().equalsIgnoreCase(snapInfo.getAncestorVolumeId())) {
								snapshot.setNativeId(snapInfo.getId());
								snapshot.setTimestamp(currentTime);
								snapshot.setAccessStatus(StorageObject.AccessStatus.READ_WRITE);
								snapshot.setDeviceLabel(snapInfo.getName());
								snapshot.setConsistencyGroup(result.getSnapshotGroupId());
								countSucc++;
							}
						}
					}
					setTaskStatus(snapshots.size(), countSucc, task);
					log.info("Create consistency group snapshot with group ID:{} - End:", consistencyGroup.getNativeId());
				} catch (Exception e) {
					log.error("Exception while Creating consistency group snapshots in storage system: {}", systemId, e);
					task.setStatus(DriverTask.TaskStatus.FAILED);
				}
			} else {
				log.error("Exception while getting Rest client instance for storage system {} ", systemId);
				task.setStatus(DriverTask.TaskStatus.FAILED);
			}
		} else {
			log.error("Snapshots are not from same storage system");
			task.setStatus(DriverTask.TaskStatus.FAILED);
		}
		task.setEndTime(Calendar.getInstance());
		log.info("Request to create consistency group snapshot -- End");
		return task;
	}

	/**
	 * Delete snapshot.
	 *
	 * @param snapshots Input.
	 * @return task
	 */
	@Override
	public DriverTask deleteConsistencyGroupSnapshot(List<VolumeSnapshot> snapshots) {
		log.info("Request to delete consistency group snapshots -- Start :");
		DriverTask task = new DriverTaskImpl(ScaleIOHelper.getTaskId(ScaleIOConstants.TaskType.CG_SNAP_DELETE));
		if (ScaleIOHelper.isFromSameCGgroup(snapshots)) {
			String systemId = snapshots.get(0).getStorageSystemId();
			log.info("Start to get Rest client for ScaleIO storage system: {}", systemId);
			ScaleIORestClient client = getClientBySystemId(systemId);
			if (client != null) {
				try {
					log.info("Rest Client Got! delete consistency group snapshot - Start:");
					client.removeConsistencyGroupSnapshot(snapshots.get(0).getConsistencyGroup());
					task.setStatus(DriverTask.TaskStatus.READY);
					log.info("Successfully delete consistency group snapshot - End:");
				} catch (Exception e) {
					log.error("Exception while deleting consistency group snapshot", e);
					task.setStatus(DriverTask.TaskStatus.FAILED);
				}
			} else {
				log.error("Exception while getting client instance for storage system {}", systemId);
				task.setStatus(DriverTask.TaskStatus.FAILED);
			}
		} else {
			log.error("Snapshots are not from same consistency group");
			task.setStatus(DriverTask.TaskStatus.FAILED);
		}
		task.setEndTime(Calendar.getInstance());
		log.info("Request to delete consistency group snapshot -- End");
		return task;
	}

	/**
	 * Create clone of consistency group.
	 *
	 * @param consistencyGroup input/output
	 * @param clones           output
	 * @param capabilities     Capabilities of clones. Type: Input.
	 * @return task
	 */
	@Override
	public DriverTask createConsistencyGroupClone(VolumeConsistencyGroup consistencyGroup, List<VolumeClone> clones,
	                                              List<CapabilityInstance> capabilities) {
		return null;
	}

	/**
	 * Delete consistency group clone
	 *
	 * @param clones output
	 * @return task
	 */
	@Override
	public DriverTask deleteConsistencyGroupClone(List<VolumeClone> clones) {
		return null;
	}

	/**
	 * Get driver registration data.
	 */
	@Override
	public RegistrationData getRegistrationData() {
		return null;
	}


	/**
	 * Discover storage systems and their capabilities
	 *
	 * @param storageSystems StorageSystems to discover. Type: Input/Output.
	 * @return
	 */
	@Override
	public DriverTask discoverStorageSystem(List<StorageSystem> storageSystems) {
		DriverTask task = new DriverTaskImpl(ScaleIOHelper.getTaskId(ScaleIOConstants.TaskType.DISCOVER_STORAGE_SYSTEM));
		for (StorageSystem storageSystem : storageSystems) {
			try {
				log.info("StorageDriver: Discovery information for storage system {}, name {} - Start", storageSystem.getIpAddress(),
						storageSystem.getSystemName());
				ScaleIORestClient scaleIOHandle = scaleIORestHandleFactory.getClientHandle(storageSystem.getSystemName(),
						storageSystem.getIpAddress(), storageSystem.getPortNumber(), storageSystem.getUsername(),
						storageSystem.getPassword());
				if (scaleIOHandle != null) {
					ScaleIOSystem scaleIOSystem = scaleIOHandle.getSystem();
					List<ScaleIOProtectionDomain> protectionDomains = scaleIOHandle.getProtectionDomains();
					for (ScaleIOProtectionDomain protectionDomain : protectionDomains) {
						String domainName = protectionDomain.getName();
						if (ScaleIOHelper.compare(domainName, storageSystem.getSystemName())) {
							storageSystem.setNativeId(protectionDomain.getId());
							storageSystem.setSystemName(domainName);
							storageSystem.setSerialNumber(protectionDomain.getSystemId());
							storageSystem.setSystemType(protectionDomain.getProtectionDomainState());
							String version = scaleIOSystem.getVersion().replaceAll("_", ".")
									.substring(ScaleIOConstants.START_POS, ScaleIOConstants.END_POS);
							storageSystem.setFirmwareVersion(version);
							if (Double.valueOf(version) < (ScaleIOConstants.MINIMUM_SUPPORTED_VERSION)) {
								storageSystem.setIsSupportedVersion(ScaleIOConstants.INCOMPATIBLE);
							} else {
								storageSystem.setIsSupportedVersion(ScaleIOConstants.COMPATIBLE);
							}
							task.setStatus(DriverTask.TaskStatus.READY);
							setConnInfoToRegistry(storageSystem.getNativeId(), storageSystem.getIpAddress(), storageSystem.getPortNumber(),
									storageSystem.getUsername(), storageSystem.getPassword());
							log.info("StorageDriver: Discovery information for storage system {}, name {} - End",
									storageSystem.getIpAddress(), storageSystem.getSystemName());
						}
					}
				} else {
					log.info("StorageDriver: Discovery failed to get an handle for the storage system {}, name {}",
							storageSystem.getIpAddress(), storageSystem.getSystemName());
					task.setStatus(DriverTask.TaskStatus.FAILED);
				}
			} catch (Exception e) {
				log.error("StorageDriver: Discovery failed for the storage system {}, name {}", storageSystem.getIpAddress(),
						storageSystem.getSystemName());
				task.setStatus(DriverTask.TaskStatus.ABORTED);
			}
		}
		return task;
	}

	/**
	 * Discover storage pools and their capabilities.
	 *
	 * @param storageSystem Type: Input.
	 * @param storagePools  Type: Output.
	 * @return
	 */
	@Override
	public DriverTask discoverStoragePools(StorageSystem storageSystem, List<StoragePool> storagePools) {
		DriverTask task = new DriverTaskImpl(ScaleIOHelper.getTaskId(ScaleIOConstants.TaskType.DISCOVER_STORAGE_POOLS));
		try {
			log.info("StorageDriver: Discovery of storage pools for storage system {}, name {} - Start", storageSystem.getIpAddress(),
					storageSystem.getSystemName());
			ScaleIORestClient scaleIOHandle = getConnInfoFromRegistry(storageSystem);
			if (scaleIOHandle != null) {
				List<ScaleIOProtectionDomain> protectionDomains = scaleIOHandle.getProtectionDomains();
				for (ScaleIOProtectionDomain protectionDomain : protectionDomains) {
					String domainID = protectionDomain.getId();
					if (ScaleIOHelper.compare(domainID, storageSystem.getNativeId())) {
						List<ScaleIOStoragePool> scaleIOStoragePoolList = scaleIOHandle.getProtectionDomainStoragePools(protectionDomain
								.getId());
						for (ScaleIOStoragePool storagePool : scaleIOStoragePoolList) {
							StoragePool pool = new StoragePool();
							pool.setNativeId(storagePool.getId());
							log.info("StorageDriver: Discovered Pool {}, storageSystem {}", pool.getNativeId(), pool.getStorageSystemId());
							pool.setStorageSystemId(protectionDomain.getId());
							pool.setPoolName(storagePool.getName());
							Set<StoragePool.Protocols> protocols = new HashSet<>();
							protocols.add(StoragePool.Protocols.FC);
							protocols.add(StoragePool.Protocols.iSCSI);
							protocols.add(StoragePool.Protocols.ScaleIO);
							pool.setProtocols(protocols);
							pool.setPoolServiceType(StoragePool.PoolServiceType.block);
							pool.setTotalCapacity(Long.valueOf(storagePool.getMaxCapacityInKb()));
							pool.setFreeCapacity(Long.valueOf(storagePool.getCapacityAvailableForVolumeAllocationInKb()));
							pool.setSupportedResourceType(StoragePool.SupportedResourceType.THIN_AND_THICK);
							pool.setOperationalStatus(StoragePool.PoolOperationalStatus.READY);
							Set<StoragePool.SupportedDriveTypes> supportedDriveTypes = new HashSet<>();
							supportedDriveTypes.add(StoragePool.SupportedDriveTypes.FC);
							supportedDriveTypes.add(StoragePool.SupportedDriveTypes.SATA);
							pool.setSupportedDriveTypes(supportedDriveTypes);
							storagePools.add(pool);
						}
					}
				}
				task.setStatus(DriverTask.TaskStatus.READY);
				log.info("StorageDriver: Discovery of storage pools for storage system {}, name {} - End", storageSystem.getIpAddress(),
						storageSystem.getSystemName());
			} else {
				log.info("StorageDriver: Failed to get an handle for the storage system {}, name {}", storageSystem.getIpAddress(),
						storageSystem.getSystemName());
				task.setStatus(DriverTask.TaskStatus.FAILED);
			}
		} catch (Exception e) {
			log.error("StorageDriver: Discovery of storage pools failed for the storage system {}, name {}", storageSystem.getIpAddress(),
					storageSystem.getSystemName());
			task.setStatus(DriverTask.TaskStatus.ABORTED);
		}
		return task;
	}

	/**
	 * Discover storage ports and their capabilities
	 *
	 * @param storageSystem Type: Input.
	 * @param storagePorts  Type: Output.
	 * @return
	 */
	@Override
	public DriverTask discoverStoragePorts(StorageSystem storageSystem, List<StoragePort> storagePorts) {
		DriverTask task = new DriverTaskImpl(ScaleIOHelper.getTaskId(ScaleIOConstants.TaskType.DISCOVER_STORAGE_PORTS));
		try {
			log.info("StorageDriver: Discovery of storage ports for storage system {}, name {} - Start", storageSystem.getNativeId(),
					storageSystem.getSystemName());
			ScaleIORestClient scaleIOHandle = getConnInfoFromRegistry(storageSystem);
			if (scaleIOHandle != null) {
				ScaleIOSystem sioSystem = scaleIOHandle.getSystem();
				String installationID = sioSystem.getInstallId();
				List<ScaleIOSDS> allSDSs = scaleIOHandle.queryAllSDS();
				for (ScaleIOSDS sds : allSDSs) {
					StoragePort port;
					String pdId = sds.getProtectionDomainId();
					if (ScaleIOHelper.compare(pdId, storageSystem.getNativeId())) {
						String sdsId = sds.getId();
						List<ScaleIOSDS.IP> ips = sds.getIpList();
						String sdsIP = null;
						if (ips != null && !ips.isEmpty()) {
							sdsIP = ips.get(0).getIp();
						}

						if (sdsId != null && ScaleIOHelper.compare(sds.getSdsState(), ScaleIOConstants.OPERATIONAL_STATUS_CONNECTED)) {
							port = new StoragePort();
							port.setNativeId(sdsId);
							log.info("StorageDriver: Discovered port {}, storageSystem {}", port.getNativeId(), port.getStorageSystemId());
							port.setDeviceLabel(String.format("%s-%s-StoragePort", sds.getName(), sdsId));
							port.setPortName(sdsId);
							port.setPortNetworkId(sdsId);
							port.setNetworkId(installationID);
							port.setStorageSystemId(storageSystem.getNativeId());
							port.setTransportType(StoragePort.TransportType.ScaleIO);
							port.setOperationalStatus(StoragePort.OperationalStatus.OK);
							port.setIpAddress(sdsIP);
							port.setPortGroup(sds.getProtectionDomainId());
							port.setPortType(StoragePort.PortType.frontend);
							storagePorts.add(port);
						}
					}
				}
				task.setStatus(DriverTask.TaskStatus.READY);
				log.info("StorageDriver: Discovery of storage ports for storage system {}, name {} - End", storageSystem.getIpAddress(),
						storageSystem.getSystemName());
			} else {
				log.info("StorageDriver: Failed to get an handle for the storage system {}, name {}", storageSystem.getIpAddress(),
						storageSystem.getSystemName());
				task.setStatus(DriverTask.TaskStatus.FAILED);
			}
		} catch (Exception e) {
			log.error("StorageDriver: Discovery of storage ports failed for the storage system {}, name {}", storageSystem.getIpAddress(),
					storageSystem.getSystemName());
			task.setStatus(DriverTask.TaskStatus.ABORTED);
		}
		return task;
	}

	@Override
	public DriverTask discoverStorageHostComponents(StorageSystem storageSystem, List<StorageHostComponent> embeddedStorageHostComponents) {
		DriverTask task = new DriverTaskImpl(ScaleIOHelper.getTaskId(ScaleIOConstants.TaskType.DISCOVER_STORAGE_HOSTS));
		try {
			log.info("StorageDriver: Discovery of storage hosts for storage system {}, name {} - Start", storageSystem.getNativeId(),
					storageSystem.getSystemName());
			ScaleIORestClient scaleIOHandle = getConnInfoFromRegistry(storageSystem);
			if (scaleIOHandle != null) {
				List<ScaleIOSDC> allSDCs = scaleIOHandle.queryAllSDC();
				for (ScaleIOSDC sdc : allSDCs) {
					StorageHostComponent host;
					if(sdc != null ) {
						host = new StorageHostComponent();
						host.setNativeId(sdc.getSdcIp());
						host.setHostName(sdc.getSdcIp());
						host.setDisplayName(sdc.getMdmConnectionState());
						Initiator initiator = new Initiator();
						initiator.setPort(sdc.getId());
						initiator.setProtocol(Initiator.Protocol.ScaleIO);
						Set<Initiator> iniSet = new HashSet<>();
						iniSet.add(initiator);
						host.setInitiators(iniSet);
						embeddedStorageHostComponents.add(host);
					}
				}
				task.setStatus(DriverTask.TaskStatus.READY);
				log.info("StorageDriver: Discovery of storage hosts for storage system {}, name {} - End", storageSystem.getIpAddress(),
						storageSystem.getSystemName());
			} else {
				log.info("StorageDriver: Failed to get an handle for the storage system {}, name {}", storageSystem.getIpAddress(),
						storageSystem.getSystemName());
				task.setStatus(DriverTask.TaskStatus.FAILED);
			}
		} catch (Exception e) {
			log.error("StorageDriver: Discovery of storage hosts failed for the storage system {}, name {}", storageSystem.getIpAddress(),
					storageSystem.getSystemName());
			task.setStatus(DriverTask.TaskStatus.ABORTED);
		}
		return task;
	}

	/**
	 * Discover storage volumes
	 *
	 * @param storageSystem  Type: Input.
	 * @param storageVolumes Type: Output.
	 * @param token          used for paging. Input 0 indicates that the first page should be returned. Output 0 indicates
	 *                       that last page was returned. Type: Input/Output.
	 * @return task
	 */
	@Override
	public DriverTask getStorageVolumes(StorageSystem storageSystem, List<StorageVolume> storageVolumes, MutableInt token) {
		return null;
	}

	/**
	 * Get list of supported storage system types. Ex. vmax, vnxblock, hitachi, etc...
	 *
	 * @return list of supported storage system types
	 */
	@Override
	public List<String> getSystemTypes() {
		return null;
	}

	/**
	 * Return driver task with a given id.
	 *
	 * @param taskId
	 * @return
	 */
	@Override
	public DriverTask getTask(String taskId) {
		return null;
	}

	/**
	 * Get storage object with a given type with specified native ID which belongs to specified storage system
	 *
	 * @param storageSystemId storage system native id
	 * @param objectId        object native id
	 * @param type            class instance
	 * @return storage object or null if does not exist
	 * <p>
	 * Example of usage: StorageVolume volume = StorageDriver.getStorageObject("vmax-12345", "volume-1234", StorageVolume.class);
	 */
	@Override
	public <T extends StorageObject> T getStorageObject(String storageSystemId, String objectId, Class<T> type) {
		return null;
	}

	/**
	 * Get connection info from registry
	 *
	 * @param systemNativeId
	 * @param attrName       use string constants in the scaleioConstants.java. e.g. ScaleIOConstants.IP_ADDRESS
	 * @return Ip_address, port, username or password for given systemId and attribute name
	 */
	public String getConnInfoFromRegistry(String systemNativeId, String attrName) {
		Map<String, List<String>> attributes = this.driverRegistry.getDriverAttributesForKey(ScaleIOConstants.DRIVER_NAME, systemNativeId);
		if (attributes == null) {
			log.info("Connection info for " + systemNativeId + " is not set up in the registry");
			return null;
		} else if (attributes.get(attrName) == null) {
			log.info(attrName + "is not found in the registry");
			return null;
		} else {
			return attributes.get(attrName).get(0);
		}
	}

	/**
	 * Set connection information to registry
	 *
	 * @param systemNativeId
	 * @param ipAddress
	 * @param port
	 * @param username
	 * @param password
	 */
	public void setConnInfoToRegistry(String systemNativeId, String ipAddress, int port, String username, String password) {
		Map<String, List<String>> attributes = new HashMap<>();
		List<String> listIP = new ArrayList<>();
		List<String> listPort = new ArrayList<>();
		List<String> listUserName = new ArrayList<>();
		List<String> listPwd = new ArrayList<>();

		listIP.add(ipAddress);
		attributes.put(ScaleIOConstants.IP_ADDRESS, listIP);
		listPort.add(Integer.toString(port));
		attributes.put(ScaleIOConstants.PORT_NUMBER, listPort);
		listUserName.add(username);
		attributes.put(ScaleIOConstants.USER_NAME, listUserName);
		listPwd.add(password);
		attributes.put(ScaleIOConstants.PASSWORD, listPwd);

		this.driverRegistry.setDriverAttributesForKey(ScaleIOConstants.DRIVER_NAME, systemNativeId, attributes);
	}

	/**
	 * get Rest Client
	 *
	 * @param systemId storage system id
	 * @return rest client handler
	 */
	private ScaleIORestClient getClientBySystemId(String systemId) {
		String ip_address, port, username, password;
		ScaleIORestClient client;
		ip_address = getConnInfoFromRegistry(systemId, ScaleIOConstants.IP_ADDRESS);
		port = getConnInfoFromRegistry(systemId, ScaleIOConstants.PORT_NUMBER);
		username = getConnInfoFromRegistry(systemId, ScaleIOConstants.USER_NAME);
		password = getConnInfoFromRegistry(systemId, ScaleIOConstants.PASSWORD);
		if (ip_address != null && port != null && username != null && password != null) {
			try {
				client = scaleIORestHandleFactory.getClientHandle(systemId, ip_address, Integer.parseInt(port), username, password);
				return client;
			} catch (Exception e) {
				log.error("Exception when creating rest client instance for storage system {} ", systemId, e);
				return null;
			}
		} else {
			log.info("Exception when retrieving connection information found for storage system {}.", systemId);
			return null;
		}
	}

	/**
	 * set task status by checking the number of successful sub-tasks.
	 *
	 * @param sizeOfTasks
	 * @param sizeOfSuccTask
	 * @param task
	 */
	private void setTaskStatus(int sizeOfTasks, int sizeOfSuccTask, DriverTask task) {
		if (sizeOfSuccTask == 0) {
			task.setStatus(DriverTask.TaskStatus.FAILED);
		} else if (sizeOfSuccTask < sizeOfTasks) {
			task.setStatus(DriverTask.TaskStatus.PARTIALLY_FAILED);
		} else {
			task.setStatus(DriverTask.TaskStatus.READY);
		}
	}

	/**
	 * Set up Driver Task for NonSupported operations
	 *
	 * @param taskType
	 * @return task
	 */
	private DriverTask setUpNonSupportedTask(ScaleIOConstants.TaskType taskType) {
		DriverTask task = new DriverTaskImpl(ScaleIOHelper.getTaskId(taskType));
		task.setStatus(DriverTask.TaskStatus.ABORTED);
		task.setMessage("Operation not supported");
		return task;
	}


	/**
	 * Get connection info from registry
	 *
	 * @param storageSystem
	 * @return ScaleIO rest client handle
	 */
	public ScaleIORestClient getConnInfoFromRegistry(StorageSystem storageSystem) {
		Map<String, List<String>> attributes = this.driverRegistry.getDriverAttributesForKey(ScaleIOConstants.DRIVER_NAME, storageSystem.getNativeId());
		String ipAddress, userName, password;
		int portNumber;
		ipAddress = getConnInfoFromRegistry(storageSystem.getNativeId(), ScaleIOConstants.IP_ADDRESS);
		portNumber = Integer.valueOf(getConnInfoFromRegistry(storageSystem.getNativeId(), ScaleIOConstants.PORT_NUMBER));
		userName = getConnInfoFromRegistry(storageSystem.getNativeId(), ScaleIOConstants.USER_NAME);
		password = getConnInfoFromRegistry(storageSystem.getNativeId(), ScaleIOConstants.PASSWORD);
		try {
			if (attributes == null || ipAddress.isEmpty() || userName.isEmpty() || password.isEmpty()) {
				log.info("StorageDriver: Connection info for " + storageSystem.getNativeId() + " is not set up in the registry");
				return null;
			} else {
				ScaleIORestClient scaleIOHandle = scaleIORestHandleFactory.getClientHandle(storageSystem.getNativeId(), ipAddress, portNumber, userName, password);
				return scaleIOHandle;
			}
		} catch (Exception e) {
			log.error("StorageDriver: Failed to get client handle for the storage system {}, name {}", storageSystem.getIpAddress(), storageSystem.getSystemName());
			return null;
		}
	}
}
