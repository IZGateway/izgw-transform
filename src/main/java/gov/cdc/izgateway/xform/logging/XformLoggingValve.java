package gov.cdc.izgateway.xform.logging;

import gov.cdc.izgateway.logging.LoggingValveBase;
import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.logging.info.SourceInfo;
import gov.cdc.izgateway.logging.markers.Markers2;
import gov.cdc.izgateway.security.service.PrincipalService;
import gov.cdc.izgateway.xform.logging.advice.XformAdviceCollector;
import jakarta.servlet.ServletException;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component("xformValveLogging")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class XformLoggingValve extends LoggingValveBase {

    @Autowired
    public XformLoggingValve(PrincipalService principalService) {
        this.principalService = principalService;
    }

    @Override
    public void invoke(Request req, Response resp) throws IOException, ServletException {
        RequestContext.setPrincipal(principalService.getPrincipal(req));

        XformTransactionData t = new XformTransactionData();

        try {
            XformAdviceCollector.setTransactionData(t);
            setSourceInfoValues(req, t);
            RequestContext.setHttpHeaders(getHeaders(req));
            if (!isLogged(req.getRequestURI())) {
                RequestContext.disableTransactionDataLogging();
            }
            if (!isLogEnabledForApi(req.getRequestURI())) {
                XformRequestContext.disableApiLogging();
            }
            handleSpecificInvoke(req, resp, t.getSource());
        } catch (Exception e) {
            log.error(Markers2.append(e), "Uncaught Exception during invocation");
        } catch (Error err) {  // NOSONAR OK to Catch Error here
            log.error(Markers2.append(err), "Error during invocation");
        } finally {
            if (XformAdviceCollector.getTransactionData() != null && !RequestContext.isLoggingDisabled()) {
                t.logIt();
            }
            XformAdviceCollector.clear();
            XformRequestContext.clear();
        }
    }

    @Override
    protected void handleSpecificInvoke(Request request, Response response, SourceInfo source) throws IOException, ServletException {
        this.getNext().invoke(request, response);
    }

    @Override
    protected boolean isLogged(String requestURI) {
        return requestURI.startsWith("/IISHubService") ||
                requestURI.startsWith("/dev/") ||
                requestURI.startsWith("/IISService");
    }

    protected boolean isLogEnabledForApi(String requestURI) {
        return requestURI.startsWith("/api");
    }
}
