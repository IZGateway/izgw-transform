package gov.cdc.izgateway.transformation.operations;

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.soap.message.SubmitSingleMessageRequest;
import gov.cdc.izgateway.soap.message.SubmitSingleMessageResponse;
import gov.cdc.izgateway.transformation.annotations.CaptureXformAdvice;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.context.XformContext;

public interface Operation {

    void execute(ServiceContext context) throws HL7Exception;
    void execute(XformContext<SubmitSingleMessageRequest, SubmitSingleMessageResponse> context) throws HL7Exception;

    void setNextOperation(Operation nextOperation);

    Operation getNextOperation();

}
