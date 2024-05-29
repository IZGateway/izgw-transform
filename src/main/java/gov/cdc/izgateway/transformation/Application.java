package gov.cdc.izgateway.transformation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.Security;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;

import org.apache.catalina.connector.Connector;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.http11.AbstractHttp11JsseProtocol;
import org.apache.tomcat.util.net.NioEndpoint;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.EntropySourceProvider;
import org.bouncycastle.crypto.fips.FipsDRBG;
import org.bouncycastle.crypto.util.BasicEntropySourceProvider;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.bouncycastle.jsse.provider.BouncyCastleJsseProvider;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
//import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import gov.cdc.izgateway.logging.event.EventId;
import gov.cdc.izgateway.logging.markers.Markers2;
import gov.cdc.izgateway.model.IDestination;
import gov.cdc.izgateway.security.SSLImplementation;
import gov.cdc.izgateway.security.ocsp.RevocationChecker;
import gov.cdc.izgateway.service.IDestinationService;
import gov.cdc.izgateway.service.IMessageHeaderService;
import gov.cdc.izgateway.soap.net.SoapMessageConverter;
import gov.cdc.izgateway.soap.net.SoapMessageWriter;
//import gov.cdc.izgateway.status.StatusCheckScheduler;
import gov.cdc.izgateway.utils.UtilizationService;
import gov.cdc.izgateway.common.HealthService;
import gov.cdc.izgateway.configuration.AppProperties;
import gov.cdc.izgateway.service.ICertificateStatusService;
//import gov.cdc.izgateway.db.service.MessageHeaderService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@OpenAPIDefinition(
        info = @Info(
                title = "IZ Gateway 2.0",
                version = "2.0",
                description = "Operations and maintenantence APIs for IZ Gateway",
                license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0"),
                contact = @Contact(url = "https://support.izgateway.org/plugins/servlet/desk/portal/3",
                        name = "IZ Gateway",
                        email = "izgateway@cdc.gov")
        )
)
@SpringBootApplication
//@EntityScan(basePackages={"gov.cdc.izgateway.db.model"})
//@ComponentScan(basePackages={"gov.cdc.izgateway.security.ocsp","gov.cdc.izgateway.service.ICertificateStatusService"})
// PAUL COMMENTED @EnableJpaRepositories(basePackages={"gov.cdc.izgateway.db.repository"})
public class Application implements WebMvcConfigurer {
    private static final Map<String, byte[]> staticPages = new TreeMap<>();
    private static final String BUILD_FILE = "build.txt";
    private static final String LOGO_FILE = "izgw-logo-16x16.ico";
    static final String BUILD = "build";
    static final String LOGO = "logo";
    private static boolean abortOnNoIIS = true;
    private static String serverMode = AppProperties.PROD_MODE_VALUE;
    private static String serverName;

    @Value("${spring.application.fix-newlines}")
    private boolean fixNewlines;

    private static SecureRandom secureRandom;

    private static AbstractHttp11JsseProtocol<?> protocol;
    public static void reloadSsl() {
        if (protocol != null) {
            protocol.reloadSslHostConfigs();
        }
    }

    public static void main(String[] args) {
        initialize();

        try {
            checkApplication(SpringApplication.run(Application.class, args));
        } catch (BeanCreationException | ApplicationContextException bce) {
            Throwable rootCause = ExceptionUtils.getRootCause(bce);
            log.error(Markers2.append(bce), "Unexpected Bean Creation Exception, Root Cause: {}", rootCause.getMessage());
            HealthService.setHealthy(rootCause);
            System.exit(1);
        } catch (Throwable ex) {  // NOSONAR Catch any error
            log.error(Markers2.append(ex), "Unexpected exception: {}", ex.getMessage());
            HealthService.setHealthy(ex);
            System.exit(1);
        }

        String build = getBuild();
        log.info("Application loaded\n{}", build);
        // FUTURE: Get from a configuration property
    }

    private static void initialize() {
        Thread.currentThread().setName("Transformation Service");


        // This is necessary initialization to use BCFKS module
        CryptoServicesRegistrar.setSecureRandom(getSecureRandom());
        Security.insertProviderAt(new BouncyCastleFipsProvider(), 1);
        Security.insertProviderAt(new BouncyCastleJsseProvider(), 2);

        // Ensure FIPS Compliance
        System.setProperty("org.bouncycastle.fips.approved_only", "true");
        // Enable renegotiation to allow servers to request client certificate after hand off from application gateway
        System.setProperty("org.bouncycastle.jsse.client.acceptRenegotiation", "true");
        // Enable JSSE Server Name Identification (SNI) connection extension in client and server connections
        System.setProperty("jsse.enableSNIExtension", "true");

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                shutdown();
            }
        });

    }

    public static void shutdown() {
        HealthService.setHealthy(false, "Service Stopped");
    }

    private static void checkApplication(ConfigurableApplicationContext ctx) {
    }

    public static String getBuild() {
        return("TODO: TSBuild");
    }

    /**
     * Generate a a NIST SP 800-90A compliant secure random number
     * generator.
     *
     * @return A compliant generator.
     */
    @Bean
    public static SecureRandom getSecureRandom() {
        /*
         * According to NIST Special Publication 800-90A, a Nonce is
         * A time-varying value that has at most a negligible chance of
         * repeating, e.g., a random value that is generated anew for each
         * use, a timestamp, a sequence number, or some combination of
         * these.
         *
         * The nonce is combined with the entropy input to create the initial
         * DRBG seed.
         */
        if (secureRandom != null) {
            return secureRandom;
        }
        byte[] nonce = ByteBuffer.allocate(8).putLong(System.nanoTime()).array();
        EntropySourceProvider entSource = new BasicEntropySourceProvider(new SecureRandom(), true);
        FipsDRBG.Builder drgbBldr = FipsDRBG.SHA512
                .fromEntropySource(entSource).setSecurityStrength(256)
                .setEntropyBitsRequired(256);
        secureRandom = drgbBldr.build(nonce, true);
        return secureRandom;
    }

    @Value("${security.enable-csrf:false}")
    private boolean enableCsrf;
    @Value("${server.local-port:9081}")
    private int additionalPort;

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
        SoapMessageConverter smc = new SoapMessageConverter(SoapMessageConverter.INBOUND);
        smc.setHub(true);
        messageConverters.add(smc);
        // Sets up SoapMessageWriter to handle \r as &#xD; if true, otherwise
        // \r in hl7Message will be replaced with \n due to XML Parsing rules.
        SoapMessageWriter.setFixNewLines(fixNewlines);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(a -> a.requestMatchers("/**").permitAll())
                .x509(Customizer.withDefaults())
                .userDetailsService(userDetailsService());
        if (!enableCsrf) {
            http.csrf(AbstractHttpConfigurer::disable);
        }
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                return new User(username, "", AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER"));
            }
        };
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer )
    {
        configurer.ignoreAcceptHeader(true).defaultContentType(MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.ALL);
    }
    @Bean
    TomcatConnectorCustomizer reloadConnectorCustomizer() {
        return Application::customizeConnector;
    }

    public static void customizeConnector(Connector connector) {
        if ("https".equals(connector.getScheme())) {
            ProtocolHandler p = connector.getProtocolHandler();
            if (p instanceof AbstractHttp11JsseProtocol<?> jsse) {
                Application.protocol = jsse;
                jsse.setSslImplementationName(SSLImplementation.class.getName());
            }
        }
    }

    @Bean
    TomcatServletWebServerFactory tomcatServletWebServerFactory(
            ObjectProvider<TomcatConnectorCustomizer> connectorCustomizers,
            ObjectProvider<TomcatContextCustomizer> contextCustomizers,
            ObjectProvider<TomcatProtocolHandlerCustomizer<?>> protocolHandlerCustomizers) {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory() {
            @Override
            public WebServer getWebServer(ServletContextInitializer... initializers) {
                log.info("Initializing Tomcat");
                try {
                    return super.getWebServer(initializers);
                } finally {
                    log.info("Tomcat initialized");
                }
            }
        };
        factory.getTomcatConnectorCustomizers().addAll(connectorCustomizers.orderedStream().toList());
        factory.getTomcatContextCustomizers().addAll(contextCustomizers.orderedStream().toList());
        factory.getTomcatProtocolHandlerCustomizers().addAll(protocolHandlerCustomizers.orderedStream().toList());
        if (additionalPort < 1) {
            return factory;
        }
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("http");
        connector.setPort(additionalPort);
        connector.setProperty("minSpareThreads", "3");  // This is for local administration, we don't need many.
        factory.addAdditionalTomcatConnectors(connector);
        return factory;
    }
}
