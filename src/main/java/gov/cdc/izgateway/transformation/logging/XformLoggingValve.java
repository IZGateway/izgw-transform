package gov.cdc.izgateway.transformation.logging;

import gov.cdc.izgateway.logging.LoggingValveBase;
import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.logging.info.SourceInfo;
import gov.cdc.izgateway.logging.markers.Markers2;
import gov.cdc.izgateway.transformation.logging.advice.XformAdviceCollector;
import jakarta.servlet.ServletException;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component("xformValveLogging")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class XformLoggingValve extends LoggingValveBase {

    @Override
    public void invoke(Request req, Response resp) throws IOException, ServletException {
        XformTransactionData t = new XformTransactionData();

        try {
            XformAdviceCollector.setTransactionData(t);
            setSourceInfoValues(req, t);
            if (!isLogged(req.getRequestURI())) {
                RequestContext.disableTransactionDataLogging();
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
        }
    }

    @Override
    protected void handleSpecificInvoke(Request request, Response response, SourceInfo source) throws IOException, ServletException {
        this.getNext().invoke(request, response);
    }

    @Override
    protected boolean isLogged(String requestURI) {
        return requestURI.startsWith("/IISHubService") || requestURI.startsWith("/dev/");
    }
}
