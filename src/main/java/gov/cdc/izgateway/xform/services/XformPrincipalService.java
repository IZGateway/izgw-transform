package gov.cdc.izgateway.xform.services;

import gov.cdc.izgateway.logging.info.HostInfo;
import gov.cdc.izgateway.principal.provider.CertificatePrincipalProvider;
import gov.cdc.izgateway.principal.provider.JwtPrincipalProvider;
import gov.cdc.izgateway.security.IzgPrincipal;
import gov.cdc.izgateway.security.UnauthenticatedPrincipal;
import gov.cdc.izgateway.security.service.PrincipalService;
import gov.cdc.izgateway.service.IAccessControlService;
import gov.cdc.izgateway.xform.model.User;
import gov.cdc.izgateway.xform.security.Roles;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

@Service
@Slf4j
public class XformPrincipalService implements PrincipalService {

    private final CertificatePrincipalProvider certificatePrincipalProvider;
    private final JwtPrincipalProvider jwtPrincipalProvider;
    private final IAccessControlService accessControlService;
    private final UserService userService;
    private final List<String> localHostIps = Arrays.asList(HostInfo.LOCALHOST_IP4, "0:0:0:0:0:0:0:1", HostInfo.LOCALHOST_IP6);
    private final GroupRoleMappingService groupRoleMappingService;

    @Autowired
    public XformPrincipalService(IAccessControlService accessControlService,
                                 CertificatePrincipalProvider certificatePrincipalProvider,
                                 JwtPrincipalProvider jwtPrincipalProvider,
                                 UserService userService,
                                 GroupRoleMappingService groupRoleMappingService) {
        this.certificatePrincipalProvider = certificatePrincipalProvider;
        this.jwtPrincipalProvider = jwtPrincipalProvider;
        this.accessControlService = accessControlService;
        this.userService = userService;
        this.groupRoleMappingService = groupRoleMappingService;
    }

    /**
     * Gets the principal from the request. This will first try to get the principal from a JWT,
     * if that fails, it will get the principal from the client certificate,
     * if that fails, it will return an UnauthenticatedPrincipal.
     * @param request The request to get the principal from
     * @return The principal from the request.  If no principal is found, an UnauthenticatedPrincipal is returned.
     */
    @Override
    public IzgPrincipal getPrincipal(HttpServletRequest request) {
        IzgPrincipal izgPrincipal = getPrincipalFromJWT(request);

        if (izgPrincipal == null) {
            izgPrincipal = getPrincipalFromCertificate(request);
        }

        if (izgPrincipal == null) {
            izgPrincipal = new UnauthenticatedPrincipal();
            // Add admin role for localhost requests, but only for the UnauthenticatedPrincipal
            addRolesIfFromLocalhost(izgPrincipal, request);
        }

        List<String> tempList = groupRoleMappingService.getRolesByGroup("IZG Operations");

        return izgPrincipal;
    }

    /**
     * Gets the principal from the JWT in the request.
     * @param request The request to get the principal from
     * @return The principal from the JWT in the request, null if no principal found
     */
    private IzgPrincipal getPrincipalFromJWT(HttpServletRequest request) {
        return jwtPrincipalProvider.createPrincipalFromJwt(request);
    }

    /**
     * Gets the principal from the client certificate in the request.
     * @param request The request to get the principal from
     * @return The principal from the client certificate in the request, null if no principal found
     */
    private IzgPrincipal getPrincipalFromCertificate(HttpServletRequest request) {
        IzgPrincipal izgPrincipal = certificatePrincipalProvider.createPrincipalFromCertificate(request);

        if (izgPrincipal == null) {
            log.warn("No principal found via certificate in the request.");
            return null;
        }

        User user = userService.getUserByUserName(izgPrincipal.getName());
        if (user == null) {
            log.warn("User not found for common name: {} - no roles will be assigned based on certificate", izgPrincipal.getName());
            return izgPrincipal;
        }

        // Add roles for the user as specified in the access control configuration
        Map<String, TreeSet<String>> userRoles = accessControlService.getUserRoles();
        TreeSet<String> roles = userRoles.get(user.getId().toString());
        if (roles == null || roles.isEmpty()) {
            log.warn("No roles found for user: {}", user.getUserName());
            return izgPrincipal;
        }

        izgPrincipal.setRoles(roles);
        log.debug("Added roles {} for user: {}", roles, user.getUserName());

        return izgPrincipal;
    }

    private void addRolesIfFromLocalhost(IzgPrincipal izgPrincipal, HttpServletRequest request) {
        if (isLocalHost(request.getRemoteHost()) && StringUtils.isEmpty(request.getHeader(Roles.NOT_ADMIN_HEADER))) {
            log.debug("Adding ADMIN role for localhost request");
            izgPrincipal.getRoles().add(Roles.ADMIN);
        }
    }

    private boolean isLocalHost(String remoteHost) {
        return localHostIps.contains(remoteHost);
    }

}