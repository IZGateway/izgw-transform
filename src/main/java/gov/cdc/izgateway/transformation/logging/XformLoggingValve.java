package gov.cdc.izgateway.transformation.logging;

import gov.cdc.izgateway.logging.LoggingValveBase;
import gov.cdc.izgateway.logging.event.TransactionData;
import gov.cdc.izgateway.logging.info.SourceInfo;
import jakarta.servlet.ServletException;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Globals;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.cert.X509Certificate;

@Slf4j
@Component("xformValveLogging")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class XformLoggingValve extends LoggingValveBase {

    @Override
    protected void handleSpecificInvoke(Request request, Response response, SourceInfo source) throws IOException, ServletException {
        this.getNext().invoke(request, response);
    }

    @Override
    protected boolean isLogged(String requestURI) {
        return requestURI.startsWith("/IISHubService") || requestURI.startsWith("/dev/");
    }

    @Override
    protected SourceInfo setSourceInfoValues(Request req, TransactionData t) {
        SourceInfo source = t.getSource();
        source.setCipherSuite((String) req.getAttribute(Globals.CIPHER_SUITE_ATTR));
        source.setHost(req.getRemoteHost());
        source.setIpAddress(req.getRemoteAddr());
        source.setType("Unknown");
        source.setFacilityId("Unknown");

        X509Certificate[] certs = (X509Certificate[])req.getAttribute(Globals.CERTIFICATES_ATTR);
        if (certs != null) {
            source.setCertificate(certs[0]);
        }
        return source;
    }
}
