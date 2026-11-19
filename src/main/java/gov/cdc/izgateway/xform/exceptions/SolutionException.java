package gov.cdc.izgateway.xform.exceptions;

/**
 * Thrown when a solution fails.
 */
public class SolutionException extends Exception {
    /**
     * Creates the exception with a message.
     *
     * @param message the detail message
     */
    public SolutionException(String message) {
        super(message);
    }

    /**
     * Creates the exception with a message and a cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public SolutionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates the exception with a cause.
     *
     * @param cause the cause
     */
    public SolutionException(Throwable cause) {
        super(cause);
    }
}
