package gov.cdc.izgateway.transformation.pipes;

import gov.cdc.izgateway.transformation.configuration.PipeConfig;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import lombok.extern.java.Log;

@Log
public class Hl7v2Pipe extends BasePipe implements Pipe {

    public Hl7v2Pipe(PipeConfig pipeConfig, ServiceContext context) {
        super(pipeConfig, context);
    }

}
