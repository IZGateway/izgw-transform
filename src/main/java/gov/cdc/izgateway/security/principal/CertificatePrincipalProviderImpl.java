package gov.cdc.izgateway.security.principal;

import gov.cdc.izgateway.principal.provider.CertificatePrincipalProvider;
import gov.cdc.izgateway.security.*;
import gov.cdc.izgateway.utils.X500Utils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Globals;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.security.auth.x500.X500Principal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

/**
 * This class provides a principal from a certificate
 */
@Slf4j
@Component
public class CertificatePrincipalProviderImpl implements CertificatePrincipalProvider {
    @Value("${client.ssl.certificate-header:}")
    private String certHeaderKey;

    private final CertificateValidator validator;

    @Autowired
    public CertificatePrincipalProviderImpl(TrustManagerProvider provider) {
        this.validator = new CertificateValidator(provider.getServerTrustManager());
    }

    @Override
    public IzgPrincipal createPrincipalFromCertificate(HttpServletRequest request) {
        X509Certificate cert = getCertificate(request);
        return cert != null ? createPrincipalFromCertificate(cert) : null;
    }

    /**
     * Gets the certificate from two possible places.  If a certificate is present in the request attribute,
     * it is returned. If not, the certificate is extracted from the header.  Null is returned if no certificate
     * is found or if the certificate is invalid.
     * @param request	The request
     * @return	The certificate
     */
    private X509Certificate getCertificate(HttpServletRequest request) {
        // Log all request headers
        log.info("Request Headers:");
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            String headerValue = request.getHeader(headerName);
            log.info("{}: {}", headerName, headerValue);
        });

        X509Certificate cert = getCertificateFromAttribute(request);
        if (cert != null) return cert;

        String certHeader = request.getHeader(certHeaderKey);
        if (StringUtils.isBlank(certHeader)) return null;

        try {
            cert = CertificateProcessor.processCertificateFromHeader(certHeader);
            return validator.isValid(cert) ? cert : null;
        } catch (CertificateException e) {
            log.error("Failed to process certificate from header", e);
            return null;
        }
    }

    private X509Certificate getCertificateFromAttribute(HttpServletRequest request) {
        X509Certificate[] certs = (X509Certificate[]) request.getAttribute(Globals.CERTIFICATES_ATTR);
        return (certs != null && certs.length > 0) ? certs[0] : null;
    }

    /**
     * Create a principal from a certificate
     * @param cert	The certificate
     * @return	The principal
     */
    public static IzgPrincipal createPrincipalFromCertificate(X509Certificate cert) {
        IzgPrincipal principal = new CertificatePrincipal();
        X500Principal subject = cert.getSubjectX500Principal();

        Map<String, String> parts = X500Utils.getParts(subject);
        principal.setName(parts.get(X500Utils.COMMON_NAME));
        String o = parts.get(X500Utils.ORGANIZATION);
        if (StringUtils.isBlank(o)) {
            o = parts.get(X500Utils.ORGANIZATION_UNIT);
        }
        principal.setOrganization(o);
        principal.setValidFrom(cert.getNotBefore());
        principal.setValidTo(cert.getNotAfter());
        principal.setSerialNumber(String.valueOf(cert.getSerialNumber()));
        principal.setIssuer(cert.getIssuerX500Principal().getName());

        return principal;
    }

}

