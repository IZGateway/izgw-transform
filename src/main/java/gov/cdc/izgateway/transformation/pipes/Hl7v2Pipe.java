package gov.cdc.izgateway.transformation.pipes;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import gov.cdc.izgateway.transformation.configuration.OperationConfig;
import gov.cdc.izgateway.transformation.configuration.OperationEqualsConfig;
import gov.cdc.izgateway.transformation.configuration.PipeConfig;
import gov.cdc.izgateway.transformation.operations.ConditionalOperation;
import gov.cdc.izgateway.transformation.operations.Hl7v2EqualsOperation;

import java.util.ArrayList;
import java.util.List;

public class Hl7v2Pipe extends BasePipe implements Pipe {

    private List<ConditionalOperation> preconditions;
    // TODO - implement Solution and add

    public Hl7v2Pipe(PipeConfig configuration) {
        super(configuration);

        // Preconditions
        // TODO - has to be a cleaner way than the if/else looking at type of class ?
        preconditions = new ArrayList<>();
        for (OperationConfig co : configuration.getPreconditions()) {
            // Precondition
            if (co instanceof OperationEqualsConfig operationEqualsConfig) {
                preconditions.add(new Hl7v2EqualsOperation(operationEqualsConfig));
            }
        }
    }

    @Override
    public void executeThisPipe(Message message) {

    }
}
