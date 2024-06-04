package gov.cdc.izgateway.transformation;

import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.EntropySourceProvider;
import org.bouncycastle.crypto.fips.FipsDRBG;
import org.bouncycastle.crypto.util.BasicEntropySourceProvider;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.bouncycastle.jsse.provider.BouncyCastleJsseProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.security.Security;

@SpringBootTest
class TransformationApplicationTests {
    private static SecureRandom secureRandom;

    @BeforeAll
    static void setup() {
        //Security.addProvider(new BouncyCastleFipsProvider());
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
    }

  @Test
  void contextLoads() {}

    @PostConstruct
    public void init() {


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
}

