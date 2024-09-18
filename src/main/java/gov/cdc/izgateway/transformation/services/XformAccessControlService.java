package gov.cdc.izgateway.transformation.services;

import gov.cdc.izgateway.security.AccessControlRegistry;
import gov.cdc.izgateway.transformation.model.AccessControl;
import gov.cdc.izgateway.transformation.repository.TxFormRepository;
//import gov.cdc.izgateway.security.AccessControlRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class XformAccessControlService extends GenericService<AccessControl> {
    public static final String ADMIN_ROLE = "admin";
    private final AccessControlRegistry registry;

    @Autowired
    public XformAccessControlService(TxFormRepository<AccessControl> repo, AccessControlRegistry registry) {
        super(repo);
        this.registry = registry;
    }

    public List<String> getAllowedRoles(RequestMethod method, String path) {
        List<String> roles = registry.getAllowedRoles(method, path);
        log.info("Roles for {} {} are {}", method, path, roles);
        return roles.isEmpty() ? Arrays.asList(ADMIN_ROLE) : roles;
    }

    public Boolean checkAccess(String user, String method, String path) {
        List<String> roles = getAllowedRoles(RequestMethod.valueOf(method), path);

        return roles.isEmpty() ? null : false;
    }

    public boolean isUserInRole(UUID organizationId, String role) {
        AccessControl accessControl = getAccessControlByOrganization(organizationId);
        return accessControl != null && Arrays.asList(accessControl.getRoles()).contains(role);
    }

    public AccessControl getAccessControlByOrganization(UUID organizationId) {
        return repo.getEntitySet().stream().filter(o -> o.getOrganizationId().equals(organizationId)).findFirst().orElse(null);
    }
}

// TODO: PCahill xform service does not manage users.  Remember that.
// TODO: PCahill create a JWT role mapping service that will map the JWT roles to the xform roles.