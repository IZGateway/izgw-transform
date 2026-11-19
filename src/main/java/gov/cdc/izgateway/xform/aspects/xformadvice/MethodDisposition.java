package gov.cdc.izgateway.xform.aspects.xformadvice;

/**
 * The point, relative to the advised method call, at which the advice aspect records advice.
 */
public enum MethodDisposition {
    /** Advice is recorded before the advised method runs. */
    PREEXECUTION,
    /** Advice is recorded after the advised method returns or throws. */
    POSTEXECUTION
}
