package gov.cdc.izgateway.xform.exceptions;

/**
 * Thrown when a solution operation fails.
 */
public class SolutionOperationException extends Exception {
    /**
     * Creates the exception with a message.
     *
     * @param message the detail message
     */
    public SolutionOperationException(String message) {
        super(message);
    }

    /**
     * Creates the exception with a message and a cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public SolutionOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates the exception with a cause.
     *
     * @param cause the cause
     */
    public SolutionOperationException(Throwable cause) {
        super(cause);
    }
}
