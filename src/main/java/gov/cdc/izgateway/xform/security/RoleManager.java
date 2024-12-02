package gov.cdc.izgateway.xform.security;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.logging.info.HostInfo;
import gov.cdc.izgateway.service.IAccessControlService;
import gov.cdc.izgateway.transformation.model.User;
import gov.cdc.izgateway.transformation.services.UserService;
import gov.cdc.izgateway.utils.X500Utils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Manages roles for the current request based on the certificate, JWT token and source IP address.
 */
@Service
@Slf4j
public class RoleManager {
    public static final String NOT_ADMIN_HEADER = "X-Not-Admin";

    private final IAccessControlService accessControlService;
    private final UserService userService;

    private final List<String> LOCAL_HOST_IPS = Arrays.asList(HostInfo.LOCALHOST_IP4, "0:0:0:0:0:0:0:1", HostInfo.LOCALHOST_IP6);

    @Value("${transformationservice.jwt-secret}")
    private String jwtSecret;

    @Autowired
    public RoleManager(IAccessControlService accessControlService, UserService userService) {
        this.accessControlService = accessControlService;
        this.userService = userService;
    }


    public void addAllRoles(HttpServletRequest request, X509Certificate[] certs) {
        addRolesUsingCert(certs);
        addRolesUsingJWT(request);
        addRolesIfFromLocalhost(request);

        log.debug("Roles assigned for the current request: {}", RequestContext.getRoles());
    }

    private void addRolesUsingCert(X509Certificate[] certs) {
        if (certs == null || certs.length == 0) {
            log.warn("No certificates found in request. No roles assigned based on certificate for this request.");
            return;
        }

        String commonName = null;

        for (X509Certificate cert : certs) {
            commonName = X500Utils.getCommonName(cert.getSubjectX500Principal());
            if (StringUtils.isNotEmpty(commonName)) {
                break;
            }
        }

        if (StringUtils.isEmpty(commonName)) {
            log.warn("No common name found in certificate");
            return;
        }

        User user = userService.getUserByUserName(commonName);
        if (user == null) {
            log.warn("User not found for common name: {}", commonName);
            return;
        }
        RequestContext.setUser(user);

        // Add roles for the user as specified in the access control configuration
        Map<String, TreeSet<String>> userRoles = accessControlService.getUserRoles();
        TreeSet<String> roles = userRoles.get(user.getId().toString());
        if (roles == null || roles.isEmpty()) {
            log.warn("No roles found for user: {}", user.getUserName());
        } else {
            RequestContext.getRoles().addAll(roles);
            log.debug("Added roles {} for user: {}", roles, user.getUserName());
        }
    }

    private void addRolesUsingJWT(HttpServletRequest request) {
        JwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri("https://izgateway1.oktapreview.com/oauth2/v1/keys").jwtProcessorCustomizer(jwtProcessor -> {
            jwtProcessor.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(new JOSEObjectType("application/okta-internal-at+jwt")));
        }).build();

        String authHeader = request.getHeader("Authorization");

        if ( authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("No JWT token found in Authorization header");
            return;
        }

        try {
            String token = authHeader.substring(7);

            log.info("*** TRYING JWT");
            Jwt jwt = jwtDecoder.decode(token);
            log.info("*** JWT claims for current request: {}", jwt.getClaims());

        } catch (Exception e) {
            log.error("Error parsing JWT token", e);
        }
    }

    private void addRolesFromClaims(Claims claims) {
        Object roles = claims.get("roles");
        if (roles instanceof List<?> rolesList) {
            for (Object roleObject : rolesList) {
                if (roleObject instanceof String role) {
                    if (Roles.isSupportedRole(role)) {
                        RequestContext.getRoles().add(role);
                        log.debug("Added role {} to current request from JWT token", role);
                    } else {
                        log.warn("Unsupported role {} found in JWT token", role);
                    }
                }
            }
        }
    }

    private void addRolesIfFromLocalhost(HttpServletRequest request) {
        if (isLocalHost(request.getRemoteHost()) && StringUtils.isEmpty(request.getHeader(NOT_ADMIN_HEADER))) {
            log.debug("Adding ADMIN role for localhost request");
            RequestContext.getRoles().add(Roles.ADMIN);
        }
    }

    private boolean isLocalHost(String remoteHost) {
        return LOCAL_HOST_IPS.contains(remoteHost);
    }
}
