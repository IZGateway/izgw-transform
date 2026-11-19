package gov.cdc.izgateway.xform.logging.advice;

/**
 * An advised object that tells the advice aspect whether to record the message as transformed.
 */
public interface Transformable {
    /**
     * Returns true if the advice must record the message as transformed after execution.
     *
     * @return true if the message is recorded as transformed
     */
    public boolean hasTransformed();
}
