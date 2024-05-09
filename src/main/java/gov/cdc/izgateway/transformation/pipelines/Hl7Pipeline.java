package gov.cdc.izgateway.transformation.pipelines;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import gov.cdc.izgateway.transformation.configuration.DataTransformationConfig;
import gov.cdc.izgateway.transformation.configuration.PipelineConfig;
import gov.cdc.izgateway.transformation.transformers.Hl7DataTransformation;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@Log
public class Hl7Pipeline extends BasePipeline implements Pipeline {

    List<Hl7DataTransformation> requestTransformations;
    List<Hl7DataTransformation> responseTransformation;


    public Hl7Pipeline(PipelineConfig pipelineConfig) {
        super(pipelineConfig);

        // So under single pipeline we will have Request & Response Transformations
        // Loop those in the pipeline config and build the objects
        requestTransformations = new ArrayList<>();
        for (DataTransformationConfig dtConfig : configuration.getRequestTransformations()) {
            requestTransformations.add(new Hl7DataTransformation(dtConfig));
        }

        responseTransformation = new ArrayList<>();
        for (DataTransformationConfig dtConfig : configuration.getResponseTransformations()) {
            responseTransformation.add(new Hl7DataTransformation(dtConfig));
        }

    }

    @Override
    public void executeThisPipeline(Message message, String direction) throws HL7Exception {
        log.log(Level.WARNING, String.format("Executing %s Pipeline: %s", direction, this.configuration.getPipelineName()));
        // TODO - finish
        // TODO - also gross w/ the direction thing definitely needs to be handled differently

        if  (direction.equals("request")) {
            log.log(Level.WARNING, String.format("Executing Request Transformation with %d operations.",
                    requestTransformations.size()));
            for (Hl7DataTransformation dataTransformation : requestTransformations) {
                dataTransformation.execute(message);
            }
        } else if (direction.equals("response")) {
            log.log(Level.WARNING, String.format("Executing Response Transformation with %d operations.",
                    responseTransformation.size()));

            for (Hl7DataTransformation dataTransformation : responseTransformation) {
                dataTransformation.execute(message);
            }
        }
    }
}