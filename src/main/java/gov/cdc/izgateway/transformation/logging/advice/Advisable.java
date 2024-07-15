package gov.cdc.izgateway.transformation.logging.advice;

public interface Advisable {
    public String getName();
    public String getId();
    public boolean hasTransformed();
    public boolean preconditionPassed();
}
