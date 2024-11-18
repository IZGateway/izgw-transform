package gov.cdc.izgateway.transformation.camel.producers;

public interface IZGComponent {
    String getDestinationUri();
    String getDestinationId();
    int getDestinationType();
}
