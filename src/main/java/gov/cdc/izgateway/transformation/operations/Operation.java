package gov.cdc.izgateway.transformation.operations;

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.transformation.context.ServiceContext;

public interface Operation {

    void execute(ServiceContext context) throws HL7Exception;

    void setNextOperation(Operation nextOperation);

    Operation getNextOperation();

}
