package gov.cdc.izgateway.security;

import gov.cdc.izgateway.logging.markers.Markers2;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.ServiceConfigurationError;

/**
 * This class provides the server trust manager for SSL connections.
 * It loads the trust store from the specified path and initializes the trust manager.
 */
@Slf4j
@Component
public class TrustManagerProvider {

    @Value("${server.ssl.trust-store:}")
    private String trustStorePath;

    @Value("${server.ssl.trust-store-password:}")
    private String trustStorePassword;

    @Value("${server.ssl.trust-store-type:}")
    private String trustStoreType;

    @Value("${server.ssl.trust-store-provider:}")
    private String trustStoreProvider;

    @Getter
    private X509TrustManager serverTrustManager;

    @PostConstruct
    private void initializeTrustManager() {
        try {
            KeyStore trustStore = KeyStore.getInstance(trustStoreType, trustStoreProvider);

            try (FileInputStream fis = new FileInputStream(trustStorePath)) {
                trustStore.load(fis, trustStorePassword.toCharArray());
            }

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX", "BCJSSE");
            tmf.init(trustStore);

            for (TrustManager tm : tmf.getTrustManagers()) {
                if (tm instanceof X509TrustManager t) {
                    this.serverTrustManager = t;
                    return;
                }
            }
        } catch (GeneralSecurityException | IOException e) {
            log.error(Markers2.append(e), "Cannot create Trust Manager from {}: {}", trustStorePath, e.getMessage());
            throw new ServiceConfigurationError(e.getMessage(), e);
        }
        throw new ServiceConfigurationError("No ServerTrustManager could be found.");
    }
}