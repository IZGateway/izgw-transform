package gov.cdc.izgateway.xform.exceptions;

public class SolutionException extends Exception {
    public SolutionException(String message) {
        super(message);
    }

    public SolutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public SolutionException(Throwable cause) {
        super(cause);
    }
}
