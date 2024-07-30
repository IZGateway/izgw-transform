package gov.cdc.izgateway.transformation.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

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
            this.getNext().invoke(req, resp);
        }
    }
    
    public boolean accessAllowed(HttpServletRequest req, HttpServletResponse resp) {
        return true;
    }
}
