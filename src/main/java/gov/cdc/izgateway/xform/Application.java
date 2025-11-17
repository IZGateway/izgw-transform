package gov.cdc.izgateway.xform;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.Security;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import gov.cdc.izgateway.common.HealthService;
import gov.cdc.izgateway.soap.net.SoapMessageConverter;
import gov.cdc.izgw.v2tofhir.utils.FhirConverter;

import gov.cdc.izgateway.utils.SystemUtils;
import org.apache.catalina.connector.Connector;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.http11.AbstractHttp11JsseProtocol;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.EntropySourceProvider;
import org.bouncycastle.crypto.fips.FipsDRBG;
import org.bouncycastle.crypto.util.BasicEntropySourceProvider;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.bouncycastle.jsse.provider.BouncyCastleJsseProvider;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
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

import gov.cdc.izgateway.security.AccessControlServiceNoop;
import gov.cdc.izgateway.security.SSLImplementation;
import gov.cdc.izgateway.service.IAccessControlService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@OpenAPIDefinition(
        info = @Info(
                title = "Xform Service",
                version = "1.0",
                description = "Operations and maintenantence APIs for Xform Service",
                license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0"),
                contact = @Contact(url = "https://support.izgateway.org/plugins/servlet/desk/portal/3",
                        name = "IZ Gateway",
                        email = "izgateway@cdc.gov")
        )
)
@SpringBootApplication
@ComponentScan(basePackages={"gov.cdc.izgateway.xform", "gov.cdc.izgateway.soap.net", "gov.cdc.izgateway.configuration","gov.cdc.izgateway.security","gov.cdc.izgateway.service.impl"})
public class Application implements WebMvcConfigurer {
    private static final Map<String, byte[]> staticPages = new TreeMap<>();
    static final String BUILD = "build";
    private static final String BUILD_FILE = "build.txt";

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
            SpringApplication.run(Application.class, args);
        } catch (BeanCreationException | ApplicationContextException bce) {
            Throwable rootCause = ExceptionUtils.getRootCause(bce);
            System.exit(1);
        } catch (Throwable ex) {  // NOSONAR Catch any error
            System.exit(1);
        }

        loadStaticResource(BUILD, BUILD_FILE);
        setInitialHealth();

        String build = getBuild();
        log.info("Xform application loaded");
        log.info("Build: {}", build);
    }

    private static void setInitialHealth() {
        HealthService.setBuildName(getBuild());
        HealthService.setHealthy(true, "Application started");
        HealthService.setServerName(SystemUtils.getHostname());
    }

    private static void initialize() {
        Thread.currentThread().setName("Xform Service");

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

    private static void loadStaticResource(String name, String location) {
        try (InputStream inStream = Application.class.getClassLoader().getResourceAsStream(location)) {
            byte[] data = IOUtils.toByteArray(inStream);
            staticPages.put(name, data);
        } catch (IOException | NullPointerException e) {
            log.error("Cannot load resource '{}' from {}", name, location);
        }
    }

    public static void shutdown() {
    }

    public static String getBuild() {
        byte[] v = staticPages.get(BUILD);
        String version = v == null ? "" : new String(v);
        return StringUtils.substringBetween(version, "Build:", "\n").trim();
    }

    public static String getPage(String page) {
        return new String(staticPages.get(page), StandardCharsets.UTF_8);
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

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
        SoapMessageConverter smc = new SoapMessageConverter(SoapMessageConverter.INBOUND);
        smc.setHub(true);
        messageConverters.add(smc);
        // FhirConverter should go before jackson converters
        // because they also handle application/json and we'd
        // prefer to use the HAPI based FHIR parsers rather
        // than the jackson ones for FHIR content.
        messageConverters.add(0, new FhirConverter());
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
    	// TODO: Check with others about this, messes up FHIR expectations.
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
    
    @Bean
    IAccessControlService accessControlServiceBean() {
		return new AccessControlServiceNoop();
	}
    
}
