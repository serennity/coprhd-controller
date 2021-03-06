/*
 * Copyright (c) 2015 EMC Corporation
 * All Rights Reserved
 */
package models.datatable;


import java.net.URI;
import java.util.Map;

import com.emc.storageos.model.RelatedResourceRep;
import com.emc.storageos.model.block.BlockSnapshotSessionRestRep;
import com.emc.storageos.model.block.VolumeRestRep;
import com.emc.vipr.client.core.util.ResourceUtils;

import util.BourneUtil;
import util.datatable.DataTable;

public class ApplicationSnapSessionDataTable extends DataTable {
	public ApplicationSnapSessionDataTable() {
		addColumn("name").setRenderFunction("renderLink");
        addColumn("subGroup");
        sortAll();
	}
	
	//Suppressing sonar violation for need of accessor methods. Accessor methods are not needed and we use public variables
	@SuppressWarnings("ClassVariableVisibilityCheck")
	public static class ApplicationSnapshotSession {
		public URI id;
		public String name;
		public String subGroup;
		
		public ApplicationSnapshotSession(BlockSnapshotSessionRestRep blockSnapSession) {
			id = blockSnapSession.getId();
			name = blockSnapSession.getName();
			subGroup = blockSnapSession.getReplicationGroupInstance();
		}
	}
}