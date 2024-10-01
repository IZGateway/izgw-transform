package gov.cdc.izgateway.transformation.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.logging.info.HostInfo;
import gov.cdc.izgateway.transformation.model.Organization;
import gov.cdc.izgateway.transformation.services.OrganizationService;
import gov.cdc.izgateway.transformation.services.XformAccessControlService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.security.cert.X509Certificate;
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

    public void addRolesUsingCert(X509Certificate[] certs) {
        if (certs == null || certs.length == 0) {
            log.warn("No certificates found in request");
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
        RequestContext.getRoles().addAll(Arrays.stream(accessControlService.getAccessControlByOrganization(org.getId()).getRoles()).toList());

        log.info("Added roles for organization: {}", org.getOrganizationName());

        // RequestContext.getRoles().add("ROLE_FROM_CERT");
    }

    //        X509Certificate[] certs = (X509Certificate[]) req.getAttribute(Globals.CERTIFICATES_ATTR);
//        String commonName = X500Utils.getCommonName(certs[0].getSubjectX500Principal());
//
//        if ( organizationService.organizationExists(commonName)) {
//            return true;
//        } else {
//            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            return false;
//        }

    /*
        {
          "sub": "pcahill",
          "name": "Paul Cahill",
          "roles": [
            "admin",
            "superuser"
          ],
          "iat": 1516239022
        }
     */
    public void addRolesUsingJWT(HttpServletRequest request) {
        log.info("Checking JWT token!!!");
        String authHeader = request.getHeader("Authorization");

        if ( authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("No JWT token found in Authorization header");
            return;
        }

        try {
            SecretKey secretKey = Keys.hmacShaKeyFor("zI2oClQyzIjQS2fQ9QLvuxM/fgN9T59M3gW6bPeliP0=".getBytes());
            String token = authHeader.substring(7);
            Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
            log.info("JWT claims 3: {}", claims);
        } catch (Exception e) {
            log.error("Error parsing JWT token", e);
        }
    }

    public void addRolesIfFromLocalhost(HttpServletRequest request) {
        log.info("getRemoteAddr: {}", request.getRemoteAddr());
        log.info("getRemoteHost: {}", request.getRemoteHost());
        if (isLocalHost(request.getRemoteHost()) && StringUtils.isEmpty(request.getHeader(Roles.NOT_ADMIN_HEADER))) {
            log.info("Adding ADMIN role for localhost request");
            RequestContext.getRoles().add(Roles.ADMIN);
        }
    }

    public void addAllRoles(HttpServletRequest request, X509Certificate[] certs) {

        addRolesUsingCert(certs);
        addRolesUsingJWT(request);
        addRolesIfFromLocalhost(request);

        log.info("Roles: {}", RequestContext.getRoles());
    }

//    private void checkJwt(HttpServletRequest request) {
//        log.info("Checking JWT token!!!");
//        String authHeader = request.getHeader("Authorization");
//
//        if ( authHeader == null || !authHeader.startsWith("Bearer ")) {
//            log.info("No JWT token found in Authorization header");
//            return;
//        }
//
//        try {
//            SecretKey secretKey = Keys.hmacShaKeyFor("zI2oClQyzIjQS2fQ9QLvuxM/fgN9T59M3gW6bPeliP0=".getBytes());
//            String token = authHeader.substring(7);
//            Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
//            log.info("JWT claims 2: {}", claims);
//        } catch (Exception e) {
//            log.error("Error parsing JWT token", e);
//        }
//    }

    private boolean isLocalHost(String remoteHost) {
        return LOCAL_HOST_IPS.contains(remoteHost);
    }
}