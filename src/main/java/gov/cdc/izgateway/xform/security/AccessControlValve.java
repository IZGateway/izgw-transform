package gov.cdc.izgateway.xform.security;

import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.xform.services.AccessControlService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * Performs access control checks.
 */
@Slf4j
@Component("xformValveAccessControl")
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class AccessControlValve extends ValveBase {
    private final AccessControlService accessControlService;

    @Autowired
    public AccessControlValve(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }

    @Override
    public void invoke(Request req, Response resp) throws IOException, ServletException {
        if (accessAllowed(req, resp)) {
            this.getNext().invoke(req, resp);
        }
    }

    public boolean accessAllowed(HttpServletRequest req, HttpServletResponse resp) {
        String path = req.getRequestURI();
        Boolean accessAllowed = accessControlService.checkAccess(req.getMethod(), path);
        if (accessAllowed == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            log.debug("Path does not exist: {}", path);
            return false;
        } else if ( Boolean.FALSE.equals(accessAllowed) ) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            log.debug("Access denied to protected URL {} address by {} at {}", 
        		path, 
        		RequestContext.getPrincipal().getName(), 
        		RequestContext.getTransactionData().getSource().getIpAddress()
            );
            return false;
        } else {
            log.debug("Access granted to protected URL {} address by {} at {}", 
        		path, 
        		RequestContext.getPrincipal().getName(), 
        		RequestContext.getTransactionData().getSource().getIpAddress()
        	);
            return true;
        }
    }
}
