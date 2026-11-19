package gov.cdc.izgateway.xform.camel.producers;

/**
 * Destination settings of a Camel component that sends messages to a downstream SOAP endpoint.
 */
public interface IZGComponent {
    /**
     * Gets the URI of the destination.
     * @return the destination URI
     */
    String getDestinationUri();
    /**
     * Gets the identifier of the destination.
     * @return the destination identifier
     */
    String getDestinationId();
    /**
     * Gets the type of the destination.
     * @return the destination type
     */
    int getDestinationType();
}
