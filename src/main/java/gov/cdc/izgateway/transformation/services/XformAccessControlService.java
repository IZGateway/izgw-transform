//package gov.cdc.izgateway.transformation.services;
//
//import gov.cdc.izgateway.logging.RequestContext;
//import gov.cdc.izgateway.security.AccessControlRegistry;
//import gov.cdc.izgateway.transformation.model.AccessControl;
//import gov.cdc.izgateway.transformation.repository.TxFormRepository;
////import gov.cdc.izgateway.security.AccessControlRegistry;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.web.bind.annotation.RequestMethod;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.UUID;
//
//@Slf4j
//@Service
//public class XformAccessControlService extends GenericService<AccessControl> {
//    private final AccessControlRegistry registry;
//
//    @Autowired
//    public XformAccessControlService(TxFormRepository<AccessControl> repo, AccessControlRegistry registry) {
//        super(repo);
//        this.registry = registry;
//    }
//
//    public List<String> getAllowedRoles(RequestMethod method, String path) {
//        List<String> roles = registry.getAllowedRoles(method, path);
//        log.debug("Roles allowed for {} {} are {}", method, path, roles);
//        return roles;
//    }
//
//    public Boolean checkAccess(String method, String path) {
//        List<String> roles = getAllowedRoles(RequestMethod.valueOf(method), path);
//
//        // If RequestContext.getRoles() has one role that matches the roles list, return true
//        return RequestContext.getRoles().stream().anyMatch(roles::contains);
//    }
//
//    public AccessControl getAccessControlByOrganization(UUID organizationId) {
//        return repo.getEntitySet().stream().filter(o -> o.getOrganizationId().equals(organizationId)).findFirst().orElse(null);
//    }
//}
