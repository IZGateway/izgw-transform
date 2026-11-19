package gov.cdc.izgateway.xform.repository;

/**
 * Thrown when the certificates or keystores for local testing cannot be created.
 */
public class CertificateBootstrapException extends Exception {
    /**
     * Creates the exception with a message.
     *
     * @param message the detail message
     */
    public CertificateBootstrapException(String message) {
        super(message);
    }

    /**
     * Creates the exception with a message and a cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public CertificateBootstrapException(String message, Throwable cause) {
        super(message, cause);
    }
}
