package gov.cdc.izgateway.transformation.endpoints.fhir;

/**
 * Wrap unexpected exceptions occurring in FhirController with this class 
 * so that they can be handled separately by an exception handler method.
 * 
 * This class enables use of @Ex
 */
class UnexpectedException extends Exception {
	private static final long serialVersionUID = 1L;

	/** 
	 * Create a new UnexpectedException 
	 * @param cause	The cause
	 **/
	public UnexpectedException(Throwable cause) {
		super(cause.getMessage(), cause);
	}
}