package com.emc.storageos.api.service.impl.resource;

import java.util.EnumSet;

import org.apache.commons.lang.StringUtils;

import com.emc.storageos.db.client.URIUtil;
import com.emc.storageos.db.client.model.CapabilityProfile;
import com.emc.storageos.db.client.model.StorageContainer.ProtocolEndpointTypeEnum;
import com.emc.storageos.db.client.model.StorageContainer.ProtocolType;
import com.emc.storageos.db.client.model.StorageContainer.ProvisioningType;
import com.emc.storageos.db.client.model.StringSet;
import com.emc.storageos.db.client.model.VirtualPool;
import com.emc.storageos.db.exceptions.DatabaseException;
import com.emc.storageos.model.vasa.CapabilityProfileCreateRequestParam;
import com.emc.storageos.model.vasa.VasaCommonRestRequest;

public abstract class AbstractCapabilityProfileService extends AbstractVasaService{

    public <T extends VasaCommonRestRequest> void populateCommonFields(VirtualPool capabilityProfile, T param) throws DatabaseException{
     // Validate the name for not null and non-empty values
        if (StringUtils.isNotEmpty(param.getName())) {
            capabilityProfile.setLabel(param.getName());
        }
        if (StringUtils.isNotEmpty(param.getDescription())) {
            capabilityProfile.setDescription(param.getDescription());
        }

    }
    
    public void populateCommonCapabilityProfileFields(VirtualPool capabilityProfile,
            CapabilityProfileCreateRequestParam param) throws DatabaseException{
        
        ArgValidator.checkFieldNotEmpty(param.getProvisioningType(), PROVISIONING_TYPE);
        ArgValidator.checkFieldValueFromEnum(param.getProvisioningType(), PROVISIONING_TYPE,
                EnumSet.of(VirtualPool.ProvisioningType.Thick, VirtualPool.ProvisioningType.Thin));

        capabilityProfile.setId(URIUtil.createId(CapabilityProfile.class));
        if (null != param.getProvisioningType()) {
            capabilityProfile.setSupportedProvisioningType(param.getProvisioningType());
        }
        
        capabilityProfile.setProtocols(new StringSet());

        // Validate the protocols for not null and non-empty values
        ArgValidator.checkFieldNotEmpty(param.getProtocols(), PROTOCOLS);
        // Validate the protocols for type of capabilityProfile.
        validateProtocol(capabilityProfile.getType(), param.getProtocols());
        capabilityProfile.getProtocols().addAll(param.getProtocols());
        
        ArgValidator.checkFieldMaximum(param.getQuotaGB(), 2000, QUOTA_GB);
        capabilityProfile.setQuota(param.getQuotaGB());
        
        if(null != param.getDriveType()){
            capabilityProfile.setDriveType(param.getDriveType());
        }
        
        if(null != param.getHighAvailability()){
            capabilityProfile.setHighAvailability(param.getHighAvailability());
        }
        
          
    }
}
