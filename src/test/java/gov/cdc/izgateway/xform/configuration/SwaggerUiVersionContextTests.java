package gov.cdc.izgateway.xform.configuration;

import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.EntropySourceProvider;
import org.bouncycastle.crypto.fips.FipsDRBG;
import org.bouncycastle.crypto.util.BasicEntropySourceProvider;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.bouncycastle.jsse.provider.BouncyCastleJsseProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.webjars.WebJarVersionLocator;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.security.Security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class SwaggerUiVersionContextTests {

    private static SecureRandom secureRandom;

    @Autowired
    private SwaggerUiConfigProperties swaggerUiConfigProperties;

    @BeforeAll
    static void setup() {
        CryptoServicesRegistrar.setSecureRandom(getSecureRandom());
        Security.insertProviderAt(new BouncyCastleFipsProvider(), 1);
        Security.insertProviderAt(new BouncyCastleJsseProvider(), 2);

        System.setProperty("org.bouncycastle.fips.approved_only", "true");
        System.setProperty("org.bouncycastle.jsse.client.acceptRenegotiation", "true");
        System.setProperty("jsse.enableSNIExtension", "true");
    }

    @Test
    void postProcessorAlignsSwaggerUiVersionInRealContext() {
        String expected = new WebJarVersionLocator().version(SwaggerUiVersionConfig.SWAGGER_UI_WEBJAR_NAME);
        assertNotNull(expected, "swagger-ui webjar must be on the test classpath");
        assertEquals(expected, swaggerUiConfigProperties.getVersion(),
                "BeanPostProcessor must override SwaggerUiConfigProperties.version during context init");
    }

    @Bean
    public static SecureRandom getSecureRandom() {
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
}
