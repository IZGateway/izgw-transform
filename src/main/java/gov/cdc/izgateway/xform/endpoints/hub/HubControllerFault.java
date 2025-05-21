package gov.cdc.izgateway.xform.endpoints.hub;

import gov.cdc.izgateway.soap.fault.Fault;
import gov.cdc.izgateway.soap.fault.MessageSupport;

/**
 * @author Audacious Inquiry
 * Represents faults related to the Transformation controller.
 */
public class HubControllerFault extends Fault {
    private static final long serialVersionUID = 1L;

    private HubControllerFault(String detail, Throwable ex) {
    	super(new MessageSupport("HubControllerFault", "500", "An error occurred while processing the request", detail, "An error occurred while processing the request", null), ex);
    }
    /** Construct a new HubControllerFault with a message */
    public HubControllerFault(String detail) {
        this(detail, null);
    }
    /** Construct a new HubControllerFault from an exception */
    public HubControllerFault(Throwable ex) {
        this(ex.getMessage(), ex);
    }
}
