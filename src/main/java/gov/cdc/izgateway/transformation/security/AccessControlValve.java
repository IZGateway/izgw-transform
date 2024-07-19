package gov.cdc.izgateway.transformation.security;

import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.logging.info.HostInfo;
import gov.cdc.izgateway.security.Roles;
import gov.cdc.izgateway.service.IAccessControlService;
import gov.cdc.izgateway.utils.SystemUtils;
import gov.cdc.izgateway.utils.X500Utils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Globals;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Performs access control checks.
 * TODO: Complete the access control checks.
 */
@Slf4j
@Component("xformValveAccessControl")
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class AccessControlValve extends ValveBase {

    @Override
    public void invoke(Request req, Response resp) throws IOException, ServletException {
        if (accessAllowed(req, resp)) {
        	try {
        		this.getNext().invoke(req, resp);
        	} finally {
        	}
        }
    }
    
    public boolean accessAllowed(HttpServletRequest req, HttpServletResponse resp) {
        return true;
    }
}
