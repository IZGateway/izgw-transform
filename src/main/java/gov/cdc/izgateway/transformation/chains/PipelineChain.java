package gov.cdc.izgateway.transformation.chains;

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.pipelines.Pipeline;
import lombok.extern.java.Log;

@Log
public class PipelineChain {

    private Pipeline firstPipeline;

    public void addPipeline(Pipeline pipeline) {
        if (firstPipeline == null) {
            firstPipeline = pipeline;
        } else {
            Pipeline currentPipeline = firstPipeline;
            while (currentPipeline.getNextPipeline() != null) {
                currentPipeline = currentPipeline.getNextPipeline();
            }
            currentPipeline.setNextPipeline(pipeline);
        }
    }

    public void execute(ServiceContext context) throws HL7Exception {
        // TODO - pipeline is singular now, remove first/next from this level
        if (firstPipeline != null) {
            firstPipeline.execute(context);
        }
    }

}
