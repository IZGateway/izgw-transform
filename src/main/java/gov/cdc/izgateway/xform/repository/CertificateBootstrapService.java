package gov.cdc.izgateway.xform.repository;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import javax.security.auth.x500.X500Principal;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;

/**
 * Certificate Bootstrap Service that runs very early in the Spring Boot lifecycle,
 * before SSL/TLS components are initialized.
 */
@Slf4j
public class CertificateBootstrapService implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    private static final String XFORM_INIT = "XFORM_INIT";
    private static final String XFORM_INIT_CERTIFICATE_PATH = "XFORM_INIT_CERTIFICATE_PATH";
    private static final String XFORM_INIT_KEY_PATH = "XFORM_INIT_KEY_PATH";
    private static final String XFORM_CRYPTO_STORE_TRUST_TOMCAT_SERVER_FILE = "XFORM_CRYPTO_STORE_TRUST_TOMCAT_SERVER_FILE";
    private static final String XFORM_CRYPTO_STORE_TRUST_WS_CLIENT_FILE = "XFORM_CRYPTO_STORE_TRUST_WS_CLIENT_FILE";

    private static final String XFORM_CRYPTO_STORE_KEY_WS_CLIENT_FILE = "XFORM_CRYPTO_STORE_KEY_WS_CLIENT_FILE";

    private static final String CERTIFICATE_ALIAS = "xform.local.testing.only";
    private static final String PROVIDER = "BCFIPS";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final String KEY_ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048;

    private String certPath;
    private String privateKeyPath;
    private String trustStorePath;
    private String trustStorePassword;
    private String clientTrustStorePath;
    private String clientTrustStorePassword;
    private String clientKeyStorePath;
    private String clientKeyStorePassword;

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        System.out.println("CertificateBootstrapService is initializing zero...");
        if (!performInitialization()) return;

        System.out.println("CertificateBootstrapService is initializing...");

        try {
            createInitialClientCertificateAndTrust();
        } catch (CertificateBootstrapException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean performInitialization() {
        String initValue = getEnv(XFORM_INIT);

        if (StringUtils.isEmpty(initValue) || !initValue.equalsIgnoreCase("true")) {
            return false;
        }

        certPath = getEnvStrict(XFORM_INIT_CERTIFICATE_PATH);
        privateKeyPath = getEnvStrict(XFORM_INIT_KEY_PATH);
        trustStorePath = getEnvStrict(XFORM_CRYPTO_STORE_TRUST_TOMCAT_SERVER_FILE);
        trustStorePassword = getEnvStrict("COMMON_PASS");
        clientTrustStorePath = getEnvStrict(XFORM_CRYPTO_STORE_TRUST_WS_CLIENT_FILE);
        clientTrustStorePassword = getEnvStrict("COMMON_PASS");
        clientKeyStorePath = getEnvStrict(XFORM_CRYPTO_STORE_KEY_WS_CLIENT_FILE);
        clientKeyStorePassword = getEnvStrict("COMMON_PASS");

        return true;
    }

    private void createInitialClientCertificateAndTrust() throws CertificateBootstrapException {
        try {
            Path certFile = Paths.get(certPath);
            Path keyFile = Paths.get(privateKeyPath);

            // Generate new certificate and key pair
            KeyPair keyPair = generateKeyPair();
            X509Certificate certificate = generateSelfSignedCertificate(keyPair);

            // Save certificate and private key
            saveCertificate(certificate, certFile);
            savePrivateKey(keyPair.getPrivate(), keyFile);

            // Update trust store
            updateTrustStore(certificate);
            updateClientTrustStore(certificate);
            createClientKeyStore(certificate, keyPair);
        } catch (Exception e) {
            throw new CertificateBootstrapException("Certificate bootstrap failed", e);
        }
    }

    private KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(KEY_ALGORITHM, PROVIDER);
        keyGen.initialize(KEY_SIZE, new SecureRandom());
        return keyGen.generateKeyPair();
    }

    private X509Certificate generateSelfSignedCertificate(KeyPair keyPair) throws Exception {
        X500Name subject = new X500Name("CN=xformclient.izgateway.org, O=izgateway");
        X500Principal principal = new X500Principal(subject.getEncoded());

        LocalDateTime notBefore = LocalDateTime.now();
        LocalDateTime notAfter = notBefore.plusYears(1);

        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                principal,
                BigInteger.valueOf(System.currentTimeMillis()),
                Date.from(notBefore.atZone(ZoneId.systemDefault()).toInstant()),
                Date.from(notAfter.atZone(ZoneId.systemDefault()).toInstant()),
                principal,
                keyPair.getPublic()
        );

        ContentSigner signer = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM)
                .setProvider(PROVIDER)
                .build(keyPair.getPrivate());

        X509CertificateHolder certHolder = certBuilder.build(signer);
        return new JcaX509CertificateConverter()
                .setProvider(PROVIDER)
                .getCertificate(certHolder);
    }

    private void saveCertificate(X509Certificate certificate, Path path) throws Exception {
        Files.createDirectories(path.getParent());
        try (PrintWriter writer = new PrintWriter(new FileWriter(path.toFile()))) {
            writer.println("-----BEGIN CERTIFICATE-----");
            writer.println(Base64.getEncoder().encodeToString(certificate.getEncoded()));
            writer.println("-----END CERTIFICATE-----");
        }
    }

    private void savePrivateKey(PrivateKey privateKey, Path path) throws Exception {
        Files.createDirectories(path.getParent());
        try (PrintWriter writer = new PrintWriter(new FileWriter(path.toFile()))) {
            writer.println("-----BEGIN RSA PRIVATE KEY-----");
            writer.println(Base64.getEncoder().encodeToString(privateKey.getEncoded()));
            writer.println("-----END RSA PRIVATE KEY-----");
        }
    }

    private void updateTrustStore(X509Certificate certificate) throws Exception {
        File trustStoreFile = new File(trustStorePath);

        // Create trust store directory if it doesn't exist
        Files.createDirectories(trustStoreFile.toPath().getParent());

        KeyStore trustStore;

        // Load existing trust store or create new one
        if (trustStoreFile.exists()) {
            trustStore = KeyStore.getInstance("BCFKS", PROVIDER);
            try (FileInputStream fis = new FileInputStream(trustStoreFile)) {
                trustStore.load(fis, trustStorePassword.toCharArray());
            }
        } else {
            trustStore = KeyStore.getInstance("BCFKS", PROVIDER);
            trustStore.load(null, trustStorePassword.toCharArray());
        }

        // Check if certificate already exists
        if (trustStore.containsAlias(CERTIFICATE_ALIAS)) {
            Certificate existingCert = trustStore.getCertificate(CERTIFICATE_ALIAS);
            if (existingCert.equals(certificate)) {
                return;
            } else {
                trustStore.deleteEntry(CERTIFICATE_ALIAS);
            }
        }

        // Add certificate to trust store
        trustStore.setCertificateEntry(CERTIFICATE_ALIAS, certificate);

        // Save trust store
        try (FileOutputStream fos = new FileOutputStream(trustStoreFile)) {
            trustStore.store(fos, trustStorePassword.toCharArray());
        }

    }

    private void updateClientTrustStore(X509Certificate certificate) throws Exception {
        File trustStoreFile = new File(clientTrustStorePath);

        // Create trust store directory if it doesn't exist
        Files.createDirectories(trustStoreFile.toPath().getParent());

        KeyStore trustStore;

        // Load existing trust store or create new one
        if (trustStoreFile.exists()) {
            trustStore = KeyStore.getInstance("BCFKS", PROVIDER);
            try (FileInputStream fis = new FileInputStream(trustStoreFile)) {
                trustStore.load(fis, clientTrustStorePassword.toCharArray());
            }
        } else {
            trustStore = KeyStore.getInstance("BCFKS", PROVIDER);
            trustStore.load(null, clientTrustStorePassword.toCharArray());
        }

        // Check if certificate already exists
        if (trustStore.containsAlias(CERTIFICATE_ALIAS)) {
            Certificate existingCert = trustStore.getCertificate(CERTIFICATE_ALIAS);
            if (existingCert.equals(certificate)) {
                return;
            } else {
                trustStore.deleteEntry(CERTIFICATE_ALIAS);
            }
        }

        // Add certificate to trust store
        trustStore.setCertificateEntry(CERTIFICATE_ALIAS, certificate);

        // Save trust store
        try (FileOutputStream fos = new FileOutputStream(trustStoreFile)) {
            trustStore.store(fos, clientTrustStorePassword.toCharArray());
        }

    }

    private void createClientKeyStore(X509Certificate certificate, KeyPair keyPair) throws Exception {
        File keyStoreFile = new File(clientKeyStorePath);

        System.out.println("Here 1 " + clientKeyStorePath);
        // Create key store directory if it doesn't exist
        Files.createDirectories(keyStoreFile.toPath().getParent());

        KeyStore keyStore;

        System.out.println("Here 2");
        // Load existing key store or create new one
        if (keyStoreFile.exists()) {
            System.out.println("Here 3");
            keyStore = KeyStore.getInstance("BCFKS", PROVIDER);
            try (FileInputStream fis = new FileInputStream(keyStoreFile)) {
                keyStore.load(fis, clientKeyStorePassword.toCharArray());
            }
        } else {
            System.out.println("Here 4");
            keyStore = KeyStore.getInstance("BCFKS", PROVIDER);
            keyStore.load(null, clientKeyStorePassword.toCharArray());
        }

        // Add certificate and private key to key store
        System.out.println("Here 6");
        keyStore.setKeyEntry(CERTIFICATE_ALIAS, keyPair.getPrivate(), clientKeyStorePassword.toCharArray(),
                new Certificate[]{certificate});

//        // Check if certificate already exists
//        if (keyStore.containsAlias(CERTIFICATE_ALIAS)) {
//            System.out.println("Here 5");
//            Certificate existingCert = keyStore.getCertificate(CERTIFICATE_ALIAS);
//            if (existingCert.equals(certificate)) {
//                return;
//            } else {
//                keyStore.deleteEntry(CERTIFICATE_ALIAS);
//            }
//        }

        // Save key store
        System.out.println("Here 7");
        try (FileOutputStream fos = new FileOutputStream(keyStoreFile)) {
            keyStore.store(fos, clientKeyStorePassword.toCharArray());
        }
    }

    private String getEnv(String variableName) {
        return System.getenv(variableName);
    }

    private String getEnvStrict(String variableName) {
        String value = getEnv(variableName);
        if (StringUtils.isEmpty(value)) {
            throw new IllegalStateException("Environment variable " + variableName + " is not set");
        }
        return value;
    }

    static {
        Security.addProvider(new BouncyCastleFipsProvider());
    }

}
