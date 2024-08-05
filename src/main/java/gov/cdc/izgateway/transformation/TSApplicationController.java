package gov.cdc.izgateway.transformation;

import gov.cdc.izgateway.transformation.services.PipelineRunnerService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Log
@RestController
public class TSApplicationController {

    private final PipelineRunnerService pipelineRunnerService;

    @Autowired
    public TSApplicationController(PipelineRunnerService pipelineRunnerService) {
        this.pipelineRunnerService = pipelineRunnerService;
    }

    @GetMapping("/hello")
    public String transform() {
        return "Hello from ApplicationController!";
    }

}
