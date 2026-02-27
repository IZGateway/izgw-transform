package gov.cdc.izgateway.xform.services;

import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.security.AccessControlRegistry;
import gov.cdc.izgateway.xform.model.AccessControl;
import gov.cdc.izgateway.xform.repository.RepositoryFactory;
import gov.cdc.izgateway.xform.security.Roles;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;
import java.util.*;

/**
 * Service for access control operations and authorization checks.
 */
@Slf4j
@Service
public class AccessControlService extends GenericService<AccessControl> {
    private final AccessControlRegistry registry;

    /**
     * Create the access control service.
     *
     * @param repositoryFactory Repository factory for access control data
     * @param registry Access control registry
     */
    @Autowired
    public AccessControlService(RepositoryFactory repositoryFactory, AccessControlRegistry registry) {
        super(repositoryFactory.accessControlRepository());
        this.registry = registry;
    }

    /**
     * Get the roles allowed for the given method and path.
     *
     * @param method HTTP method
     * @param path Request path
     * @return List of allowed roles
     */
    public List<String> getAllowedRoles(RequestMethod method, String path) {
        List<String> roles = registry.getAllowedRoles(method, path);
        log.debug("Roles allowed for {} {} are {}", method, path, roles);
        return roles;
    }

    /**
     * Check whether access is allowed for the given method and path.
     *
     * @param method HTTP method
     * @param path Request path
     * @return True if access is allowed
     */
    public Boolean checkAccess(String method, String path) {
        // If path starts with /swagger, return checkSwaggerAccess, else return checkXformAccess
        if (path.startsWith("/swagger")) {
            return checkSwaggerAccess(method, path);
        } else {
            return checkXformAccess(method, path);
        }
    }

    /**
     * Check if the user has access to the swagger endpoints.  This method is necessary because the swagger
     * library and endpoints will not register with our AccessControlRegistry.
     * @param method HTTP method
     * @param path Request path
     * @return true if access is allowed, false if access is denied
     */
    public Boolean checkSwaggerAccess(String method, String path) {
        return path.startsWith("/swagger") && RequestContext.getPrincipal().getRoles().contains(Roles.ADMIN);
    }

    /**
     * Check if the user has access to the xform endpoints.
     * @param method HTTP method
     * @param path Request path
     * @return true if access is allowed, false if access is denied
     */
    public Boolean checkXformAccess(String method, String path) {
        List<String> allowedRoles = getAllowedRoles(RequestMethod.valueOf(method), path);

        // Check for public access
        if (allowedRoles.contains(Roles.PUBLIC_ACCESS)) {
            return true;
        }

        // If RequestContext.getRoles() has one role that matches the roles list, return true
        return RequestContext.getPrincipal().getRoles().stream().anyMatch(allowedRoles::contains);
    }

    /**
     * Build a mapping of user ids to their assigned roles.
     *
     * @return Map of user id to roles
     */
    public Map<String, TreeSet<String>> getUserRoles() {
        Map<String, TreeSet<String>> userRoleMap = new HashMap<>();
        for (AccessControl ac : repo.getEntitySet()) {
            userRoleMap.put(ac.getUserId().toString(), new TreeSet<>(ac.getRoles()));
        }

        return userRoleMap;
    }

    /**
     * Checks if an access control with the same user id already exists
     *
     * @param accessControl The Access Control to check for duplication
     * @return true if a duplicate exists, false otherwise
     */
    @Override
    protected boolean isDuplicate(AccessControl accessControl) {
        return repo.getEntitySet().stream()
                .anyMatch(ac ->
                        ac.getUserId().equals(accessControl.getUserId())
                );
    }
}
