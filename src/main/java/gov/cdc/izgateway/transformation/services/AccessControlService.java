package gov.cdc.izgateway.transformation.services;

import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.model.IAccessControl;
import gov.cdc.izgateway.security.AccessControlRegistry;
import gov.cdc.izgateway.service.IAccessControlService;
import gov.cdc.izgateway.transformation.model.AccessControl;
import gov.cdc.izgateway.transformation.repository.TxFormRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.*;

/**
 * The AccessControlService is a placeholder for the IAccessControlService interface.  We will be implementing
 * a simpler repository than the existing DB repository.
 * Ticket to track this: https://support.izgateway.org/browse/IGDD-1664
 */
@Slf4j
@Service
public class AccessControlService  extends GenericService<AccessControl> implements IAccessControlService {
    private final AccessControlRegistry registry;

    @Autowired
    public AccessControlService(TxFormRepository<AccessControl> repo, AccessControlRegistry registry) {
        super(repo);
        this.registry = registry;
    }

    public List<String> getAllowedRoles(RequestMethod method, String path) {
        List<String> roles = registry.getAllowedRoles(method, path);
        log.debug("Roles allowed for {} {} are {}", method, path, roles);
        return roles;
    }

    @Override
    public Boolean checkAccess(String user, String method, String path) {
        List<String> roles = getAllowedRoles(RequestMethod.valueOf(method), path);

        // If RequestContext.getRoles() has one role that matches the roles list, return true
        return RequestContext.getRoles().stream().anyMatch(roles::contains);
    }

//    public Boolean checkAccess(String method, String path) {
//        List<String> roles = getAllowedRoles(RequestMethod.valueOf(method), path);
//
//        // If RequestContext.getRoles() has one role that matches the roles list, return true
//        return RequestContext.getRoles().stream().anyMatch(roles::contains);
//    }

    @Override
    public Map<String, TreeSet<String>> getUserRoles() {
        Map<String, TreeSet<String>> userRoleMap = new HashMap<>();
        for (AccessControl ac : repo.getEntitySet()) {
            userRoleMap.put(ac.getOrganizationId().toString(), new TreeSet<>(List.of(ac.getRoles())));
        }

        return userRoleMap;
    }


    // TODO: PCahill - instead use core - IAccessControlService.getUserRoles
//    public AccessControl getAccessControlByOrganization(UUID organizationId) {
//        return repo.getEntitySet().stream().filter(o -> o.getOrganizationId().equals(organizationId)).findFirst().orElse(null);
//    }
    /////////////////////////

    @Override
    public String getServerName() {
        return "";
    }

    @Override
    public void refresh() {
    }

    @Override
    public Map<String, Map<String, Boolean>> getAllowedUsersByGroup() {
        return Map.of();
    }

    @Override
    public Map<String, Map<String, Boolean>> getAllowedRoutesByEvent() {
        return Map.of();
    }

    @Override
    public boolean isUserInRole(String user, String role) {
        return false;
    }

    @Override
    public boolean isUserBlacklisted(String user) {
        return false;
    }

    @Override
    public Map<String, Boolean> getEventMap(String event) {
        return Map.of();
    }

    @Override
    public Set<String> getEventTypes() {
        return Set.of();
    }

    @Override
    public boolean isRouteAllowed(String route, String event) {
        return false;
    }

    @Override
    public void setServerName(String serverName) {
    }

    @Override
    public boolean isMemberOf(String user, String group) {
        return false;
    }

    @Override
    public IAccessControl removeUserFromBlacklist(String user) {
        return null;
    }

    @Override
    public IAccessControl addUserToBlacklist(String user) {
        return null;
    }

}
// TODO: PCahill create a JWT role mapping service that will map the JWT roles to the xform roles.
// TODO: Where to store the actual user?
// TODO: Compare and align access control valve
// TODO: PCahill - make sure org override works
// TODO: PCahill - make sure roles are cleared after request