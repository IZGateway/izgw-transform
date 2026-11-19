package gov.cdc.izgateway.xform.enums;

/**
 * The direction of a message through a pipeline.
 */
public enum DataFlowDirection {
    /** The inbound request, sent to the destination. */
    REQUEST,
    /** The response from the destination; pipes run in reverse order. */
    RESPONSE
}
