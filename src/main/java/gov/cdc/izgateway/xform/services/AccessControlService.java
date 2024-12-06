package gov.cdc.izgateway.xform.services;

import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.security.AccessControlRegistry;
import gov.cdc.izgateway.xform.model.AccessControl;
import gov.cdc.izgateway.xform.repository.XformRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;
import java.util.*;

@Slf4j
@Service
public class AccessControlService extends GenericService<AccessControl> {
    private final AccessControlRegistry registry;

    @Autowired
    public AccessControlService(XformRepository<AccessControl> repo, AccessControlRegistry registry) {
        super(repo);
        this.registry = registry;
    }

    public List<String> getAllowedRoles(RequestMethod method, String path) {
        List<String> roles = registry.getAllowedRoles(method, path);
        log.debug("Roles allowed for {} {} are {}", method, path, roles);
        return roles;
    }

    public Boolean checkAccess(String method, String path) {
        List<String> allowedRoles = getAllowedRoles(RequestMethod.valueOf(method), path);

        // If RequestContext.getRoles() has one role that matches the roles list, return true
        return RequestContext.getPrincipal().getRoles().stream().anyMatch(allowedRoles::contains);
    }

    public Map<String, TreeSet<String>> getUserRoles() {
        Map<String, TreeSet<String>> userRoleMap = new HashMap<>();
        for (AccessControl ac : repo.getEntitySet()) {
            userRoleMap.put(ac.getUserId().toString(), new TreeSet<>(List.of(ac.getRoles())));
        }

        return userRoleMap;
    }
}
