package gov.cdc.izgateway.xform.camel.producers;

public interface IZGComponent {
    String getDestinationUri();
    String getDestinationId();
    int getDestinationType();
}
