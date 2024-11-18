package gov.cdc.izgateway.xform.endpoints.fhir;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;

import com.ainq.fhir.utils.PathUtils;

import gov.cdc.izgateway.model.RetryStrategy;
import gov.cdc.izgateway.soap.message.FaultMessage;
import lombok.Getter;

/**
 * This class creates an exception from a FaultMessage and
 * provided access to the FaultMessage content.
 */
class FaultException extends Exception {
	private static final long serialVersionUID = 1L;
	@Getter
	private final FaultMessage fault;
	/**
	 * Create a new FaultException from a FaultMessage
	 * @param fault	The FaultMessage
	 */
	public FaultException(FaultMessage fault) {
		super(getMessage(fault));
		this.fault = fault;
	}
	
	private static String getMessage(FaultMessage fault) {
		if (StringUtils.isBlank(fault.getDetail())) {
			return fault.getSummary();
		}
		return fault.getSummary() + ": " + fault.getDetail();
	}

	RetryStrategy getRetryCoding() {
    	try {
    		return RetryStrategy.valueOf(fault.getRetry());
    	} catch (Exception ex) {
    		// Do nothing, we don't recognize the retry code.
    		// Preferable to suppress this "error" than the one that is being reported by this code
    		return null;
    	}
	}
	
	void setOriginalText(OperationOutcomeIssueComponent issue) {
		String original = fault.getOriginal();
    	if (StringUtils.isNotBlank(original)) {
    		issue.getDetails()
    			.addExtension(
    				new Extension(
    					PathUtils.FHIR_EXT_PREFIX + "originalText",
    					new StringType(original)
    				)
    			);
    	}
	}
		
	void setEventId(OperationOutcomeIssueComponent issue) {
		// Give them the eventId from the fault
    	if (StringUtils.isNotEmpty(fault.getEventId())) {
			issue.addExtension(
				new Extension(
					PathUtils.FHIR_EXT_PREFIX + "operationoutcome-message-id",
					new StringType(fault.getEventId())
				)
			);
    	}
	}		
}
