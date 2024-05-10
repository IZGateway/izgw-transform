package gov.cdc.izgateway.transformation.pipes;

import ca.uhn.hl7v2.model.Message;
import gov.cdc.izgateway.transformation.configuration.*;
import gov.cdc.izgateway.transformation.operations.ConditionalOperation;
import gov.cdc.izgateway.transformation.operations.Hl7v2EqualsOperation;
import gov.cdc.izgateway.transformation.solutions.Solution;
import jakarta.annotation.PostConstruct;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Log
public class Hl7v2Pipe extends BasePipe implements Pipe {

    private List<ConditionalOperation> preconditions;
    private Solution solution;

//    @Autowired
//    private ServiceConfig serviceConfig;

//    private List<SolutionConfig> solutionConfigs;
//
//    @PostConstruct
//    public void init() {
//        this.solutionConfigs = serviceConfig.getSolutions();
//    }

    public Hl7v2Pipe(PipeConfig configuration) throws Exception {
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

        // Get Solution configuration from full system configuration
//        Optional<SolutionConfig> solutionConfig = solutionConfigs.stream()
//                .filter(sc -> sc.getId().equals(configuration.getSolutionId()))
//                .findFirst();
//
//        if (solutionConfig.isPresent()) {
//            solution = new Solution(solutionConfig.get());
//        } else {
//            throw new Exception(String.format("Solution not found in system with ID %s", configuration.getSolutionId()));
//        }
    }

    @Override
    public void executeThisPipe(Message message) {

    }
}
