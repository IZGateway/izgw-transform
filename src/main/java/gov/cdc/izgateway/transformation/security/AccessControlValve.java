package gov.cdc.izgateway.transformation.security;

import gov.cdc.izgateway.transformation.services.OrganizationService;
import gov.cdc.izgateway.transformation.services.XformAccessControlService;
import gov.cdc.izgateway.utils.X500Utils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Globals;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.cert.X509Certificate;

/**
 * Performs access control checks.
 */
@Slf4j
@Component("xformValveAccessControl")
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class AccessControlValve extends ValveBase {
    private final OrganizationService organizationService;
    private final XformAccessControlService accessControlService;
    private final RoleManager roleManager;

    @Value("${transformationservice.access-control-enabled}")
    private boolean accessControlEnabled;

    @Autowired
    public AccessControlValve(OrganizationService organizationService, XformAccessControlService accessControlService, RoleManager roleManager) {
        this.organizationService = organizationService;
        this.accessControlService = accessControlService;
        this.roleManager = roleManager;
    }

    @Override
    public void invoke(Request req, Response resp) throws IOException, ServletException {
        if (accessAllowed(req, resp)) {
            this.getNext().invoke(req, resp);
        }
    }
    
    public boolean accessAllowed(HttpServletRequest req, HttpServletResponse resp) {
        // TODO: Paul - consider taking this out if we have everything covered using the RoleManager
        if (!accessControlEnabled) {
            return true;
        }

        roleManager.addAllRoles(req, (X509Certificate[]) req.getAttribute(Globals.CERTIFICATES_ATTR));

        String path = req.getRequestURI();

        // TODO: Paul, after all the roles are set, then look at the org override
        //      * Add roles for the organization specified in the override header only if the request is from localhost.

        if ( ! accessControlService.checkAccess(req.getMethod(), path) ) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        } else {
            return true;
        }

        // moved this to RoleManager checkJwt(req);


        // TODO get the following into RoleManager
//        X509Certificate[] certs = (X509Certificate[]) req.getAttribute(Globals.CERTIFICATES_ATTR);
//        String commonName = X500Utils.getCommonName(certs[0].getSubjectX500Principal());
//
//        if ( organizationService.organizationExists(commonName)) {
//            return true;
//        } else {
//            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            return false;
//        }

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
//            log.info("JWT claims: {}", claims);
//        } catch (Exception e) {
//            log.error("Error parsing JWT token", e);
//        }
//    }

}
