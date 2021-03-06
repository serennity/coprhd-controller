/*
 * Copyright (c) 2015 EMC Corporation
 * All Rights Reserved
 */
package util;


import java.net.URI;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import static com.emc.vipr.client.core.util.ResourceUtils.uri;

import com.emc.storageos.model.NamedRelatedResourceRep;
import com.emc.storageos.model.TaskList;
import com.emc.storageos.model.application.VolumeGroupCopySetParam;
import com.emc.storageos.model.application.VolumeGroupCreateParam;
import com.emc.storageos.model.application.VolumeGroupList;
import com.emc.storageos.model.application.VolumeGroupRestRep;
import com.emc.storageos.model.application.VolumeGroupUpdateParam;


/**
 * Utility for application support.
 * 
 * @author hr2
 */

public class AppSupportUtil {
    
    public static List<NamedRelatedResourceRep> getApplications() {
        return BourneUtil.getViprClient().application().getApplications().getVolumeGroups();
    }
    
    public static VolumeGroupRestRep createApplication(String name, String description, Set<String> roles){
        VolumeGroupCreateParam create = new VolumeGroupCreateParam();
        create.setName(name);
        if (!StringUtils.isBlank(description)) {
            create.setDescription(description);
        }
        create.setRoles(roles);
        return BourneUtil.getViprClient().application().createApplication(create);
    }
    
    public static void deleteApplication(URI id) {
        BourneUtil.getViprClient().application().deleteApplication(id);
    }
    
    public static TaskList updateApplication(String name, String description, String id) {
        VolumeGroupUpdateParam update = new VolumeGroupUpdateParam();
        if(!name.isEmpty()) {
            update.setName(name);
        }
        if(!description.isEmpty()) {
            update.setDescription(description);
        }
        return BourneUtil.getViprClient().application().updateApplication(uri(id), update);
    }
    
    public static VolumeGroupRestRep getApplication(String id) {
        return BourneUtil.getViprClient().application().getApplication(uri(id));
    }
    
    public static List<NamedRelatedResourceRep> getVolumesByApplication(String id) {
        return BourneUtil.getViprClient().application().getVolumeByApplication(uri(id)).getVolumes();
    }
    
    public static List<NamedRelatedResourceRep> getFullCopiesByApplication(String id) {
    	return BourneUtil.getViprClient().application().getClonesByApplication(uri(id)).getVolumes();
    }
    
    public static Set<String> getVolumeGroupSnapsetSessionSets(String id) {
        return BourneUtil.getViprClient().application().getVolumeGroupSnapsetSessionSets(uri(id)).getCopySets();
    }
    
    public static List<NamedRelatedResourceRep> getVolumeGroupSnapshotSessionsByCopySet(String id, String sessionSet) {
        VolumeGroupCopySetParam snapshotSessionSet = new VolumeGroupCopySetParam();
        snapshotSessionSet.setCopySetName(sessionSet);
        return BourneUtil.getViprClient().application().getVolumeGroupSnapshotSessionsByCopySet(uri(id), snapshotSessionSet)
                .getSnapSessionRelatedResourceList();
    }
    
    public static Set<String> getVolumeGroupSnapshotSets(String id) {
        return BourneUtil.getViprClient().application().getVolumeGroupSnapshotSets(uri(id)).getCopySets();
    }

    public static List<NamedRelatedResourceRep> getVolumeGroupSnapshotsForSet(String id, String snapSet) {
        VolumeGroupCopySetParam newParam = new VolumeGroupCopySetParam();
        newParam.setCopySetName(snapSet);
        return BourneUtil.getViprClient().application().getVolumeGroupSnapshotsForSet(uri(id), newParam).getSnapList();
    }

    public static Set<String> getFullCopySetsByApplication(String id) {
        return BourneUtil.getViprClient().application().getFullCopySetsByApplication(uri(id)).getCopySets();
    }
    
    public static List<NamedRelatedResourceRep> getVolumeGroupFullCopiesForSet(String id, String copySets) {
        VolumeGroupCopySetParam getSetsForCopies = new VolumeGroupCopySetParam();
        getSetsForCopies.setCopySetName(copySets);
        return BourneUtil.getViprClient().application().getVolumeGroupFullCopiesForSet(uri(id), getSetsForCopies).getVolumes();
    }

	public static List<NamedRelatedResourceRep> getVolumeGroupByTenant(String id) {
		return BourneUtil.getViprClient().application()
				.getVolumeGroupsByTenant(uri(id)).getVolumeGroups();
	}
}
