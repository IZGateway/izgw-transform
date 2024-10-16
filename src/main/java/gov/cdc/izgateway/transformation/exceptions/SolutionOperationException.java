package gov.cdc.izgateway.transformation.exceptions;

public class SolutionOperationException extends Exception {
    public SolutionOperationException(String message) {
        super(message);
    }

    public SolutionOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SolutionOperationException(Throwable cause) {
        super(cause);
    }
}
