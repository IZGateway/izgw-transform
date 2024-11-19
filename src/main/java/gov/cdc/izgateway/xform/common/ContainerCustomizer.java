package gov.cdc.izgateway.xform.common;

import gov.cdc.izgateway.xform.logging.XformLoggingValve;
import gov.cdc.izgateway.xform.security.AccessControlValve;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ContainerCustomizer implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

	private XformLoggingValve loggingValve;
	private AccessControlValve accessControlValve;
	
	 @Autowired
	 public ContainerCustomizer(XformLoggingValve loggingValve, AccessControlValve accessControlValve) {
		this.loggingValve = loggingValve;
		this.accessControlValve = accessControlValve;
	}
	
    @Override
    public void customize(TomcatServletWebServerFactory factory) {

        log.info("Configuring embedded Tomcat");
        factory.addContextValves(loggingValve, accessControlValve);
    }
}
