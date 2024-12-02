package gov.cdc.izgateway.xform.security;

import gov.cdc.izgateway.service.IAccessControlService;
import gov.cdc.izgateway.xform.services.OrganizationService;
import gov.cdc.izgateway.utils.X500Utils;
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

import java.io.IOException;
import java.security.cert.X509Certificate;

/**
 * Performs access control checks.
 */
@Slf4j
@Component("xformValveAccessControl")
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class AccessControlValve extends ValveBase {
    private final IAccessControlService accessControlService;
    private final RoleManager roleManager;

    @Value("${xform.access-control-enabled}")
    private boolean accessControlEnabled;

    @Autowired
    public AccessControlValve(IAccessControlService accessControlService, RoleManager roleManager) {
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
        roleManager.addAllRoles(req, (X509Certificate[]) req.getAttribute(Globals.CERTIFICATES_ATTR));

        String path = req.getRequestURI();

        if ( ! accessControlService.checkAccess("Talk through this during review", req.getMethod(), path) ) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        } else {
            return true;
        }
    }
}
