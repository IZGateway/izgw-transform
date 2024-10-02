package gov.cdc.izgateway.transformation.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.logging.info.HostInfo;
import gov.cdc.izgateway.transformation.model.AccessControl;
import gov.cdc.izgateway.transformation.model.Organization;
import gov.cdc.izgateway.transformation.services.OrganizationService;
import gov.cdc.izgateway.transformation.services.XformAccessControlService;
import gov.cdc.izgateway.transformation.util.Utils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.security.cert.X509Certificate;
import java.util.function.Function;

import gov.cdc.izgateway.utils.X500Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

@Service
@Slf4j
public class RoleManager {
    private final OrganizationService organizationService;
    private final XformAccessControlService accessControlService;
    private final List<String> LOCAL_HOST_IPS = Arrays.asList(HostInfo.LOCALHOST_IP4, "0:0:0:0:0:0:0:1", HostInfo.LOCALHOST_IP6);

    @Autowired
    public RoleManager(OrganizationService organizationService, XformAccessControlService accessControlService) {
        this.organizationService = organizationService;
        this.accessControlService = accessControlService;
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

        Organization org = organizationService.getOrganizationByCommonName(commonName);
        if (org == null) {
            log.warn("Organization not found for common name: {}", commonName);
            return;
        }

        // Add roles for the organization as specified in the access control configuration
        AccessControl accessControl = accessControlService.getAccessControlByOrganization(org.getId());
//        if (accessControl != null && accessControl.getRoles() != null && accessControl.getRoles().length > 0) {
//            RequestContext.getRoles().addAll(Arrays.stream(accessControl.getRoles()).toList());
//            log.debug("Added roles {} for organization: {}", accessControl.getRoles(), org.getOrganizationName());
//        } else {
//            log.warn("No roles found for organization: {}", org.getOrganizationName());
//        }

        if (Utils.isEmpty(accessControl, AccessControl::getRoles)) {
            log.warn("No roles found for organization: {}", org.getOrganizationName());
        } else {
            RequestContext.getRoles().addAll(Arrays.stream(accessControl.getRoles()).toList());
            log.debug("Added roles {} for organization: {}", accessControl.getRoles(), org.getOrganizationName());
        }
    }

//    private <T, R> boolean isEmpty(T obj, Function<T, R> mapper) {
//        if (obj == null) {
//            return true;
//        }
//        R result = mapper.apply(obj);
//        if (result == null) {
//            return true;
//        }
//        if (result instanceof Object[] && ((Object[]) result).length == 0) {
//            return true;
//        }
//        return false;
//    }

    private void addRolesUsingJWT(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if ( authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("No JWT token found in Authorization header");
            return;
        }

        try {
            SecretKey secretKey = Keys.hmacShaKeyFor("zI2oClQyzIjQS2fQ9QLvuxM/fgN9T59M3gW6bPeliP0=".getBytes());
            String token = authHeader.substring(7);
            Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
            log.debug("JWT claims for current request: {}", claims);

            Object roles = claims.get("roles");
            if(roles instanceof List<?> rolesList) {
                for (Object role : rolesList) {
                    RequestContext.getRoles().add(role.toString());
                }
            }

        } catch (Exception e) {
            log.error("Error parsing JWT token", e);
        }
    }

    private void addRolesIfFromLocalhost(HttpServletRequest request) {
        if (isLocalHost(request.getRemoteHost()) && StringUtils.isEmpty(request.getHeader(Roles.NOT_ADMIN_HEADER))) {
            log.debug("Adding ADMIN role for localhost request");
            RequestContext.getRoles().add(Roles.ADMIN);
        }
    }

    private boolean isLocalHost(String remoteHost) {
        return LOCAL_HOST_IPS.contains(remoteHost);
    }
}