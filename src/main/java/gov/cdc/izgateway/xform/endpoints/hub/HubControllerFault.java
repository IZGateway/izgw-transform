package gov.cdc.izgateway.xform.endpoints.hub;

import org.apache.commons.lang3.StringUtils;

import gov.cdc.izgateway.model.RetryStrategy;
import gov.cdc.izgateway.soap.fault.Fault;
import gov.cdc.izgateway.soap.fault.MessageSupport;

/**
 * @author Audacious Inquiry
 * Represents faults related to the Transformation controller.
 */
public class HubControllerFault extends Fault {
    private static final long serialVersionUID = 1L;

    private HubControllerFault(String detail, Throwable ex) {
    	// For now, at least while in Pilot, a this Fault should indicate a CONTACT_SUPPORT RetryStrategy.
    	super(new MessageSupport("HubControllerFault", "500", getMessage(detail), getDetail(detail), null, RetryStrategy.CONTACT_SUPPORT), ex);
    }
    /** Construct a new HubControllerFault with a message */
    public HubControllerFault(String detail) {
        this(detail, null);
    }
    /** Construct a new HubControllerFault from an exception */
    public HubControllerFault(Throwable ex) {
        this(ex.getMessage(), ex);
    }
    
    private static String getMessage(String message) {
    	return StringUtils.defaultIfEmpty(StringUtils.substringBefore(message, ":"), "An error occurred while processing the request");
    }
    
    private static String getDetail(String message) {
    	return StringUtils.defaultIfEmpty(StringUtils.substringAfter(message, ":"), message);
    }

}
