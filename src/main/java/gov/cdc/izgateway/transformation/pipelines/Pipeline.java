package gov.cdc.izgateway.transformation.pipelines;

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.transformation.context.ServiceContext;

public interface Pipeline {

    // TODO - if throw stays needs to be generic, not everything will be HL7
    void execute(ServiceContext context) throws HL7Exception;

}
