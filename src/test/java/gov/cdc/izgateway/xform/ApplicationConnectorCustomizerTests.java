package gov.cdc.izgateway.xform;

import gov.cdc.izgateway.security.SSLImplementation;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Pins the Tomcat 11 connector customization done by
 * {@link Application#customizeConnector(Connector)}.
 *
 * The HTTPS connector must be handed izgw-core's BouncyCastle FIPS
 * SSLImplementation. If the protocol-handler type check stops matching -- as it
 * would have when Tomcat 11 removed AbstractHttp11JsseProtocol in favour of
 * AbstractHttp11Protocol -- the connector silently falls back to Tomcat's own
 * JSSE implementation and FIPS compliance is lost without any error. (IGDD-2353)
 */
class ApplicationConnectorCustomizerTests {
    private static final String HTTP11_NIO = "org.apache.coyote.http11.Http11NioProtocol";

    @Test
    void testHttpsConnectorGetsFipsSslImplementation() {
        Connector connector = new Connector(HTTP11_NIO);
        connector.setScheme("https");

        Application.customizeConnector(connector);

        AbstractHttp11Protocol<?> protocol =
                assertInstanceOf(AbstractHttp11Protocol.class, connector.getProtocolHandler());
        assertEquals(SSLImplementation.class.getName(), protocol.getSslImplementationName());
    }

    @Test
    void testPlainHttpConnectorIsLeftAlone() {
        Connector connector = new Connector(HTTP11_NIO);
        connector.setScheme("http");

        Application.customizeConnector(connector);

        AbstractHttp11Protocol<?> protocol =
                assertInstanceOf(AbstractHttp11Protocol.class, connector.getProtocolHandler());
        assertNotEquals(SSLImplementation.class.getName(), protocol.getSslImplementationName());
    }
}
