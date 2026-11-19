package gov.cdc.izgateway.xform.repository;

/**
 * Checked exception for repository errors.
 */
public class RepositoryException extends Exception {
    /**
     * Creates the exception with a message and a cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
