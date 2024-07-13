package gov.cdc.izgateway.transformation.logging.advice;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gov.cdc.izgateway.transformation.enums.DataFlowDirection;

/**
 * This record represents a transformation advice.  Transformation advice is used to log the state of a transformation
 * as it is being processed.  This data will be used to create header records in the response messages.  The advice
 * shows the consumer the state of the transformation at the time of the advice.
 *
 * @param className The class name of the object performing the transformation.
 * @param methodName The method name of the object performing the transformation.
 * @param methodDisposition The disposition of the method (pre- or post- execution).
 * @param descriptor A description of the object performing the transformation.  This is typically the name of the
 *                   pipeline or transformation solution being used to perform the transformation.
 * @param requestMessage The request message being transformed.
 * @param responseMessage The response message being transformed.
 */
public record XformAdvice(String className, String methodName, MethodDisposition methodDisposition, String descriptor,
                          String descriptorId, /* TODO @JsonIgnore */ String requestMessage, /* TODO @JsonIgnore */ String responseMessage, DataFlowDirection dataFlowDirection) {

}
