package gov.cdc.izgateway.xform.logging;

import gov.cdc.izgateway.logging.LoggingValveBase;
import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.logging.event.TransactionData;
import gov.cdc.izgateway.logging.info.SourceInfo;
import gov.cdc.izgateway.logging.markers.Markers2;
import gov.cdc.izgateway.security.service.PrincipalService;
import gov.cdc.izgateway.xform.logging.advice.XformAdviceCollector;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@Component("xformValveLogging")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class XformLoggingValve extends LoggingValveBase {

    @Autowired
    public XformLoggingValve(PrincipalService principalService) {
        this.principalService = principalService;
    }

    @Override
	protected void clearContext() {
		XformAdviceCollector.clear();
		XformRequestContext.clear();
	}

    @Override
    protected void handleSpecificInvoke(Request request, Response response, SourceInfo source) throws IOException, ServletException {
	    if (!isLogEnabledForApi(request.getRequestURI())) {
	        XformRequestContext.disableApiLogging();
	    }
    	this.getNext().invoke(request, response);
    	if (Arrays.asList(HttpServletResponse.SC_UNAUTHORIZED, HttpServletResponse.SC_SERVICE_UNAVAILABLE).contains(response.getStatus())) {
            // In these two cases, someone tried to access IZGW via a URL they shouldn't have.  
    		// There was never a transaction to begin with. 
            RequestContext.disableTransactionDataLogging();
    	}
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

	@Override
	protected TransactionData createTransactionData(Request req) {
        XformTransactionData t = new XformTransactionData();
        XformAdviceCollector.setTransactionData(t);
		return t;
	}
}
