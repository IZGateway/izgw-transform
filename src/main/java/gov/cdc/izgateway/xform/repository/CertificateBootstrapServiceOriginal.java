//package gov.cdc.izgateway.xform.repository;
//
//import gov.cdc.izgateway.security.TrustManagerProvider;
//import org.bouncycastle.asn1.x500.X500Name;
//import org.bouncycastle.cert.X509CertificateHolder;
//import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
//import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
//import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
//import org.bouncycastle.operator.ContentSigner;
//import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.boot.context.event.ApplicationReadyEvent;
//import org.springframework.context.event.EventListener;
//import org.springframework.stereotype.Service;
//
//import javax.security.auth.x500.X500Principal;
//import java.io.*;
//import java.math.BigInteger;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.security.*;
//import java.security.cert.Certificate;
//import java.security.cert.X509Certificate;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.util.Base64;
//import java.util.Date;
//import java.util.logging.Logger;
//
//@Service
//@ConditionalOnProperty(name = "init", havingValue = "true")
//public class CertificateBootstrapServiceOriginal {
//
//    private static final Logger logger = Logger.getLogger(CertificateBootstrapServiceOriginal.class.getName());
//    private static final String CERTIFICATE_ALIAS = "xformclient-mtls";
//    private static final String PROVIDER = "BCFIPS";
//    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
//    private static final String KEY_ALGORITHM = "RSA";
//    private static final int KEY_SIZE = 2048;
//
//    @Value("${mtls.certificate.path:/users/cahilp/certs/client-cert.pem}")
//    private String certPath;
//
//    @Value("${mtls.privatekey.path:/users/cahilp/certs/client-key.pem}")
//    private String privateKeyPath;
//
//    @Value("${XFORM_CRYPTO_STORE_TRUST_TOMCAT_SERVER_FILE:/users/cahilp/certs/truststore.bcfks}")
//    private String trustStorePath;
//
//    @Value("${COMMON_PASS:changeit}")
//    private String trustStorePassword;
//
//    static {
//        Security.addProvider(new BouncyCastleFipsProvider());
//    }
//
//    private final TrustManagerProvider trustManagerProvider;
//
//    public CertificateBootstrapServiceOriginal(@Autowired TrustManagerProvider trustManagerProvider) {
//        this.trustManagerProvider = trustManagerProvider;
//    }
//
//    @EventListener(ApplicationReadyEvent.class)
//    public void bootstrapCertificate() {
//        logger.info("Starting mTLS certificate bootstrap process...");
//
//        try {
//            Path certFile = Paths.get(certPath);
//            Path keyFile = Paths.get(privateKeyPath);
//
//            // Check if certificate already exists
//            if (Files.exists(certFile) && Files.exists(keyFile)) {
//                logger.info("Certificate files already exist. Verifying and updating trust store...");
//                X509Certificate existingCert = loadExistingCertificate(certFile);
//                updateTrustStore(existingCert);
//                return;
//            }
//
//            // Generate new certificate and key pair
//            logger.info("Generating new self-signed certificate...");
//            KeyPair keyPair = generateKeyPair();
//            X509Certificate certificate = generateSelfSignedCertificate(keyPair);
//
//            // Save certificate and private key
//            saveCertificate(certificate, certFile);
//            savePrivateKey(keyPair.getPrivate(), keyFile);
//
//            // Update trust store
//            updateTrustStore(certificate);
//
//            logger.info("Certificate bootstrap completed successfully");
//
//        } catch (Exception e) {
//            logger.severe("Failed to bootstrap certificate: " + e.getMessage());
//            throw new RuntimeException("Certificate bootstrap failed", e);
//        }
//    }
//
//    private KeyPair generateKeyPair() throws Exception {
//        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(KEY_ALGORITHM, PROVIDER);
//        keyGen.initialize(KEY_SIZE, new SecureRandom());
//        return keyGen.generateKeyPair();
//    }
//
//    private X509Certificate generateSelfSignedCertificate(KeyPair keyPair) throws Exception {
//        X500Name subject = new X500Name("CN=xformclient.izgateway.org, O=izgateway");
//        X500Principal principal = new X500Principal(subject.getEncoded());
//
//        LocalDateTime notBefore = LocalDateTime.now();
//        LocalDateTime notAfter = notBefore.plusYears(1);
//
//        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
//                principal,
//                BigInteger.valueOf(System.currentTimeMillis()),
//                Date.from(notBefore.atZone(ZoneId.systemDefault()).toInstant()),
//                Date.from(notAfter.atZone(ZoneId.systemDefault()).toInstant()),
//                principal,
//                keyPair.getPublic()
//        );
//
//        ContentSigner signer = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM)
//                .setProvider(PROVIDER)
//                .build(keyPair.getPrivate());
//
//        X509CertificateHolder certHolder = certBuilder.build(signer);
//        return new JcaX509CertificateConverter()
//                .setProvider(PROVIDER)
//                .getCertificate(certHolder);
//    }
//
//    private void saveCertificate(X509Certificate certificate, Path path) throws Exception {
//        Files.createDirectories(path.getParent());
//        try (PrintWriter writer = new PrintWriter(new FileWriter(path.toFile()))) {
//            writer.println("-----BEGIN CERTIFICATE-----");
//            writer.println(Base64.getEncoder().encodeToString(certificate.getEncoded()));
//            writer.println("-----END CERTIFICATE-----");
//        }
//        logger.info("Certificate saved to: " + path);
//    }
//
//    private void savePrivateKey(PrivateKey privateKey, Path path) throws Exception {
//        Files.createDirectories(path.getParent());
//        try (PrintWriter writer = new PrintWriter(new FileWriter(path.toFile()))) {
//            writer.println("-----BEGIN RSA PRIVATE KEY-----");
//            writer.println(Base64.getEncoder().encodeToString(privateKey.getEncoded()));
//            writer.println("-----END RSA PRIVATE KEY-----");
//        }
//        // Set restrictive permissions on private key file
//        path.toFile().setReadable(false, false);
//        path.toFile().setReadable(true, true);
//        path.toFile().setWritable(false, false);
//        path.toFile().setWritable(true, true);
//        logger.info("Private key saved to: " + path);
//    }
//
//    private X509Certificate loadExistingCertificate(Path path) throws Exception {
//        String certContent = Files.readString(path);
//        certContent = certContent.replace("-----BEGIN CERTIFICATE-----", "")
//                .replace("-----END CERTIFICATE-----", "")
//                .replaceAll("\\s", "");
//
//        byte[] certBytes = Base64.getDecoder().decode(certContent);
//        java.security.cert.CertificateFactory cf =
//                java.security.cert.CertificateFactory.getInstance("X.509");
//        return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certBytes));
//    }
//
//    private void updateTrustStore(X509Certificate certificate) throws Exception {
//        File trustStoreFile = new File(trustStorePath);
//
//        // Create trust store directory if it doesn't exist
//        Files.createDirectories(trustStoreFile.toPath().getParent());
//
//        KeyStore trustStore;
//
//        // Load existing trust store or create new one
//        if (trustStoreFile.exists()) {
//            logger.info("Loading existing BCFKS trust store...");
//            trustStore = KeyStore.getInstance("BCFKS", PROVIDER);
//            try (FileInputStream fis = new FileInputStream(trustStoreFile)) {
//                trustStore.load(fis, trustStorePassword.toCharArray());
//            }
//        } else {
//            logger.info("Creating new BCFKS trust store...");
//            trustStore = KeyStore.getInstance("BCFKS", PROVIDER);
//            trustStore.load(null, trustStorePassword.toCharArray());
//        }
//
//        // Check if certificate already exists
//        if (trustStore.containsAlias(CERTIFICATE_ALIAS)) {
//            Certificate existingCert = trustStore.getCertificate(CERTIFICATE_ALIAS);
//            if (existingCert.equals(certificate)) {
//                logger.info("Certificate already exists in trust store with alias: " + CERTIFICATE_ALIAS);
//                return;
//            } else {
//                logger.info("Updating existing certificate in trust store...");
//                trustStore.deleteEntry(CERTIFICATE_ALIAS);
//            }
//        }
//
//        // Add certificate to trust store
//        trustStore.setCertificateEntry(CERTIFICATE_ALIAS, certificate);
//
//        // Save trust store
//        try (FileOutputStream fos = new FileOutputStream(trustStoreFile)) {
//            trustStore.store(fos, trustStorePassword.toCharArray());
//        }
//
//        logger.info("Certificate added to trust store with alias: " + CERTIFICATE_ALIAS);
//    }
//}