package gov.cdc.izgateway.xform.repository;

public class CertificateBootstrapException extends Exception {
    public CertificateBootstrapException(String message) {
        super(message);
    }

    public CertificateBootstrapException(String message, Throwable cause) {
        super(message, cause);
    }
}
