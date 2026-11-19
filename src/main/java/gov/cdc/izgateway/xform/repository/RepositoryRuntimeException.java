package gov.cdc.izgateway.xform.repository;

/**
 * Unchecked exception for errors that occur when a repository reads or writes entities.
 */
public class RepositoryRuntimeException extends RuntimeException {
    /**
     * Creates the exception with a message and a cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public RepositoryRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
