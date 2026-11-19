package gov.cdc.izgateway.xform.exceptions;

/**
 * Thrown when an operation fails.
 */
public class OperationException extends Exception {
    /**
     * Creates the exception with a message.
     *
     * @param message the detail message
     */
    public OperationException(String message) {
        super(message);
    }

    /**
     * Creates the exception with a message and a cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public OperationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates the exception with a cause.
     *
     * @param cause the cause
     */
    public OperationException(Throwable cause) {
        super(cause);
    }
}

