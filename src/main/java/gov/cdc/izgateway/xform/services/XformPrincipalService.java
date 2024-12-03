package gov.cdc.izgateway.xform.services;

import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.principal.provider.CertificatePrincipalProvider;
import gov.cdc.izgateway.principal.provider.JwtPrincipalProvider;
import gov.cdc.izgateway.security.IzgPrincipal;
import gov.cdc.izgateway.security.UnauthenticatedPrincipal;
import gov.cdc.izgateway.security.service.PrincipalService;
import gov.cdc.izgateway.service.IAccessControlService;
import gov.cdc.izgateway.xform.model.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.TreeSet;

@Service
@Slf4j
public class XformPrincipalService implements PrincipalService {

    private final CertificatePrincipalProvider certificatePrincipalProvider;
    private final JwtPrincipalProvider jwtPrincipalProvider;
    private final IAccessControlService accessControlService;
    private final UserService userService;

    @Autowired
    public XformPrincipalService(IAccessControlService accessControlService,
                                 CertificatePrincipalProvider certificatePrincipalProvider,
                                 JwtPrincipalProvider jwtPrincipalProvider,
                                 UserService userService) {
        this.certificatePrincipalProvider = certificatePrincipalProvider;
        this.jwtPrincipalProvider = jwtPrincipalProvider;
        this.accessControlService = accessControlService;
        this.userService = userService;
    }

    /**
     * Get the principal from the request. This will first try to get the principal from the certificate, if that fails, it will return an UnauthenticatedPrincipal.
     * @param request
     * @return
     */
    @Override
    public IzgPrincipal getPrincipal(HttpServletRequest request) {
        if (request == null)
            return null;

        IzgPrincipal izgPrincipal = getPrincipalFromJWT(request);
        if (izgPrincipal != null) {
            return izgPrincipal;
        }

        izgPrincipal = getPrincipalFromCertificate(request);
        if (izgPrincipal != null) {
            return izgPrincipal;
        }

        return new UnauthenticatedPrincipal();
    }

    private IzgPrincipal getPrincipalFromJWT(HttpServletRequest request) {
        return jwtPrincipalProvider.createPrincipalFromJwt(request);
    }

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
}