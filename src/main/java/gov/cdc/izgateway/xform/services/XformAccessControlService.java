package gov.cdc.izgateway.xform.services;

import gov.cdc.izgateway.xform.model.AccessControl;
import gov.cdc.izgateway.xform.repository.TxFormRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;

@Service
public class XformAccessControlService extends GenericService<AccessControl> {
    public static final String ADMIN_ROLE = "admin";

    @Autowired
    public XformAccessControlService(TxFormRepository<AccessControl> repo) {
        super(repo);
    }

    public boolean isUserInRole(UUID organizationId, String role) {
        AccessControl accessControl = getAccessControlByOrganization(organizationId);
        return accessControl != null && Arrays.asList(accessControl.getRoles()).contains(role);
    }

    public AccessControl getAccessControlByOrganization(UUID organizationId) {
        return repo.getEntitySet().stream().filter(o -> o.getOrganizationId().equals(organizationId)).findFirst().orElse(null);
    }
}

