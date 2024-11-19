package gov.cdc.izgateway.xform.endpoints.hub;

import gov.cdc.izgateway.soap.fault.Fault;
import gov.cdc.izgateway.soap.fault.MessageSupport;

public class HubControllerFault extends Fault {
    private static final long serialVersionUID = 1L;

    public HubControllerFault(String detail) {
        super(new MessageSupport("HubControllerFault", "500", "An error occurred while processing the request", detail, "An error occurred while processing the request", null));
    }
}
