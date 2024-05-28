package gov.cdc.izgateway.transformation.pipelines;

import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

@Service
@Log
public class NewPipelineBuilder {

//    private ServiceConfig serviceConfig;
//
//    @Autowired
//    public NewPipelineBuilder(ServiceConfig serviceConfig) {
//        this.serviceConfig = serviceConfig;
//    }

    public Hl7Pipeline build () {
        // this is being called from when a message comes in
        // and passing in the context which has the org id, inbound, and outbound

        // First off we have "from the context" these
        // - org id
        // - inbound id
        // - outbound id

        // We will from the above have a "Pipeline"


        return new Hl7Pipeline();

    }
}
