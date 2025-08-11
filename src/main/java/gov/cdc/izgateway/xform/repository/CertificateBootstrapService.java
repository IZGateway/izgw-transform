package gov.cdc.izgateway.xform.repository;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.operator.OperatorCreationException;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.lang.Nullable;

import javax.security.auth.x500.X500Principal;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;

/**
 * This class is used to bootstrap certificates and keystores/truststores for local testing.
 * It is only activated if the environment variable XFORM_INIT is set to true.
 * It starts prior to Spring context initialization to ensure that the keystores/truststores are available
 * for the application.  This is done by specifying this class in the META-INF/spring.factories file.
 */
@Slf4j
public class CertificateBootstrapService implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    private static final String XFORM_INIT = "XFORM_INIT";
    private static final String XFORM_CRYPTO_CLIENT_CERT_FILE = "XFORM_CRYPTO_CLIENT_CERT_FILE";
    private static final String XFORM_CRYPTO_CLIENT_KEY_FILE = "XFORM_CRYPTO_CLIENT_KEY_FILE";
    private static final String XFORM_CRYPTO_STORE_TRUST_TOMCAT_SERVER_FILE = "XFORM_CRYPTO_STORE_TRUST_TOMCAT_SERVER_FILE";
    private static final String XFORM_CRYPTO_STORE_TRUST_WS_CLIENT_FILE = "XFORM_CRYPTO_STORE_TRUST_WS_CLIENT_FILE";
    private static final String XFORM_CRYPTO_STORE_KEY_WS_CLIENT_FILE = "XFORM_CRYPTO_STORE_KEY_WS_CLIENT_FILE";
    private static final String COMMON_PASS = "COMMON_PASS";

    private static final String CERTIFICATE_ALIAS = "xform.local.testing.only";
    private static final String PROVIDER = "BCFIPS";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final String KEY_ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048;
    private static final String KEYSTORE_TYPE = "BCFKS";

    private String certPath;
    private String privateKeyPath;
    private String trustStorePath;
    private String trustStorePassword;
    private String clientTrustStorePath;
    private String clientTrustStorePassword;
    private String clientKeyStorePath;
    private String clientKeyStorePassword;

    private volatile boolean initialized = false;

    /**
     * Handles the ApplicationEnvironmentPreparedEvent to trigger certificate and keystore/truststore initialization.
     * @param event the Spring application environment prepared event
     */
    @Override
    public void onApplicationEvent(@Nullable ApplicationEnvironmentPreparedEvent event) {
        if (!performInitialization()) return;

        log.info("CertificateBootstrapService is initializing...");

        try {
            createInitialClientCertificateAndTrust();
        } catch (CertificateBootstrapException e) {
            throw new RuntimeException(e);
        }

        log.info("CertificateBootstrapService has completed.");
    }

    /**
     * Checks if initialization should be performed and loads required environment variables.
     * @return true if initialization should proceed, false otherwise
     */
    private boolean performInitialization() {
        if (initialized) {
            log.debug("CertificateBootstrapService already initialized, skipping...");
            return false;
        }

        String initValue = getEnv(XFORM_INIT);

        if (StringUtils.isEmpty(initValue) || !initValue.equalsIgnoreCase("true")) {
            return false;
        }

        certPath = getEnvStrict(XFORM_CRYPTO_CLIENT_CERT_FILE);
        privateKeyPath = getEnvStrict(XFORM_CRYPTO_CLIENT_KEY_FILE);
        trustStorePath = getEnvStrict(XFORM_CRYPTO_STORE_TRUST_TOMCAT_SERVER_FILE);
        trustStorePassword = getEnvStrict(COMMON_PASS);
        clientTrustStorePath = getEnvStrict(XFORM_CRYPTO_STORE_TRUST_WS_CLIENT_FILE);
        clientTrustStorePassword = getEnvStrict(COMMON_PASS);
        clientKeyStorePath = getEnvStrict(XFORM_CRYPTO_STORE_KEY_WS_CLIENT_FILE);
        clientKeyStorePassword = getEnvStrict(COMMON_PASS);

        return true;
    }

    /**
     * Creates the initial client certificate, saves it and the private key, and updates trust/key stores.
     * @throws CertificateBootstrapException if any error occurs during the process
     */
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

            // Update trust stores and create key store
            addCertificateToTrustStore(certificate, trustStorePath, trustStorePassword);
            addCertificateToTrustStore(certificate, clientTrustStorePath, clientTrustStorePassword);
            addCertificateAndKeyToKeyStore(certificate, keyPair, clientKeyStorePath, clientKeyStorePassword);

            initialized = true;
        } catch (Exception e) {
            throw new CertificateBootstrapException("Certificate bootstrap failed", e);
        }
    }

    /**
     * Loads an existing keystore or creates a new one if it does not exist.
     * @param keyStorePath the path to the keystore file
     * @param password the keystore password
     * @return the loaded or newly created KeyStore
     * @throws CertificateBootstrapException if loading or creation fails
     */
    private KeyStore loadOrCreateKeyStore(String keyStorePath, String password) throws CertificateBootstrapException {
        try {
            File keyStoreFile = new File(keyStorePath);
            Files.createDirectories(keyStoreFile.toPath().getParent());

            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE, PROVIDER);

            if (keyStoreFile.exists()) {
                try (FileInputStream fis = new FileInputStream(keyStoreFile)) {
                    keyStore.load(fis, password.toCharArray());
                }
            } else {
                keyStore.load(null, password.toCharArray());
            }

            return keyStore;

        } catch (IOException | NoSuchProviderException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            throw new CertificateBootstrapException("Failed to load or create KeyStore", e);
        }
    }

    /**
     * Saves the given KeyStore to the specified file path.
     * @param keyStore the KeyStore to save
     * @param keyStorePath the file path to save the KeyStore
     * @param password the password for the KeyStore
     * @throws CertificateBootstrapException if saving fails
     */
    private void saveKeyStore(KeyStore keyStore, String keyStorePath, String password) throws CertificateBootstrapException {
        try {
            FileOutputStream fos = new FileOutputStream(keyStorePath);
            keyStore.store(fos, password.toCharArray());
            log.info("KeyStore saved to {}", keyStorePath);

        } catch(IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            throw new CertificateBootstrapException("Failed to save KeyStore", e);
        }
    }

    /**
     * Adds the given certificate to the specified trust store.
     * @param certificate the X509 certificate to add
     * @param trustStorePath the trust store file path
     * @param password the trust store password
     * @throws CertificateBootstrapException if the operation fails
     */
    private void addCertificateToTrustStore(X509Certificate certificate, String trustStorePath, String password) throws CertificateBootstrapException {
        try {
            KeyStore trustStore = loadOrCreateKeyStore(trustStorePath, password);

            if (trustStore.containsAlias(CERTIFICATE_ALIAS)) {
                Certificate existingCert = trustStore.getCertificate(CERTIFICATE_ALIAS);
                if (existingCert.equals(certificate)) {
                    return;
                } else {
                    // Remove existing certificate if different
                    trustStore.deleteEntry(CERTIFICATE_ALIAS);
                }
            }

            trustStore.setCertificateEntry(CERTIFICATE_ALIAS, certificate);
            saveKeyStore(trustStore, trustStorePath, password);
        } catch (KeyStoreException e) {
            throw new CertificateBootstrapException("Failed to add certificate to trust store", e);
        }
    }

    /**
     * Adds the given certificate and private key to the specified key store.
     * @param certificate the X509 certificate to add
     * @param keyPair the key pair containing the private key
     * @param keyStorePath the key store file path
     * @param password the key store password
     * @throws CertificateBootstrapException if the operation fails
     */
    private void addCertificateAndKeyToKeyStore(X509Certificate certificate, KeyPair keyPair, String keyStorePath, String password) throws CertificateBootstrapException {
        try {
            KeyStore keyStore = loadOrCreateKeyStore(keyStorePath, password);
            keyStore.setKeyEntry(CERTIFICATE_ALIAS, keyPair.getPrivate(), password.toCharArray(), new Certificate[]{certificate});
            saveKeyStore(keyStore, keyStorePath, password);
        } catch (KeyStoreException e) {
            throw new CertificateBootstrapException("Failed to add cert and key to keystore", e);
        }
    }

    /**
     * Generates a new RSA key pair using the Bouncy Castle FIPS provider.
     * @return the generated KeyPair
     * @throws CertificateBootstrapException if key generation fails
     */
    private KeyPair generateKeyPair() throws CertificateBootstrapException {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(KEY_ALGORITHM, PROVIDER);
            keyGen.initialize(KEY_SIZE, new SecureRandom());
            return keyGen.generateKeyPair();
        } catch (NoSuchProviderException | NoSuchAlgorithmException e) {
            throw new CertificateBootstrapException("Failed to add Bouncy Castle FIPS provider", e);
        }
    }

    /**
     * Generates a self-signed X509 certificate for the given key pair.
     * @param keyPair the key pair to use for the certificate
     * @return the generated X509Certificate
     * @throws CertificateBootstrapException if certificate generation fails
     */
    private X509Certificate generateSelfSignedCertificate(KeyPair keyPair) throws CertificateBootstrapException {
        try {
            X500Name subject = new X500Name("CN=xform.local.testing.only, O=izgateway");
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
        } catch (IOException | OperatorCreationException | CertificateException e) {
            throw new CertificateBootstrapException("Failed to generate self-signed certificate", e);
        }
    }

    /**
     * Saves the given X509 certificate to a file in PEM format.
     * @param certificate the certificate to save
     * @param path the file path to save the certificate
     * @throws CertificateBootstrapException if saving fails
     */
    private void saveCertificate(X509Certificate certificate, Path path) throws CertificateBootstrapException {
        try {
            Files.createDirectories(path.getParent());
            PrintWriter writer = new PrintWriter(new FileWriter(path.toFile()));
            writer.println("-----BEGIN CERTIFICATE-----");
            writer.println(Base64.getEncoder().encodeToString(certificate.getEncoded()));
            writer.println("-----END CERTIFICATE-----");
            writer.close();
            log.info("Certificate saved to {}", path);
        } catch (IOException | CertificateEncodingException e) {
            throw new CertificateBootstrapException("Failed to save certificate file", e);
        }

    }

    /**
     * Saves the given private key to a file in PEM format.
     * @param privateKey the private key to save
     * @param path the file path to save the private key
     * @throws CertificateBootstrapException if saving fails
     */
    private void savePrivateKey(PrivateKey privateKey, Path path) throws CertificateBootstrapException {
        try {
            Files.createDirectories(path.getParent());
            PrintWriter writer = new PrintWriter(new FileWriter(path.toFile()));
            writer.println("-----BEGIN RSA PRIVATE KEY-----");
            writer.println(Base64.getEncoder().encodeToString(privateKey.getEncoded()));
            writer.println("-----END RSA PRIVATE KEY-----");
            writer.close();

            log.info("Private key saved to {}", path);
        } catch (IOException e) {
            throw new CertificateBootstrapException("Failed to create directories for private key file", e);
        }
    }

    /**
     * Get the value of an environment variable.
     * System.getenv() is used because Spring is not fully initialized yet.
     * @param variableName The environment variable name to retrieve.
     * @return The value of the environment variable, or null if not set.
     */
    private String getEnv(String variableName) {
        return System.getenv(variableName);
    }

    /**
     * Get the value of an environment variable and will throw an exception if it is not found.
     * System.getenv() is used because Spring is not fully initialized yet.
     * @param variableName The environment variable name to retrieve.
     * @return The value of the environment variable, or null if not set.
     */
    private String getEnvStrict(String variableName) {
        String value = getEnv(variableName);
        if (StringUtils.isEmpty(value)) {
            throw new IllegalStateException("Environment variable " + variableName + " is not set");
        }
        return value;
    }
}