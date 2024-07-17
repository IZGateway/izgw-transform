package gov.cdc.izgateway.transformation.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cdc.izgateway.transformation.model.Pipeline;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.UUID;

@Repository
public class PipelineFileRepository implements PipelineRepository {

    private LinkedHashSet<Pipeline> pipelines;

    @Value("${transformationservice.configurations.pipelines}")
    private String pipelinesFilePath;

    @Override
    public Pipeline getPipeline(UUID id) {
        return getPipelineSet().stream().filter(p -> p.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public LinkedHashSet<Pipeline> getPipelineSet() {
        if (pipelines == null) {
            ObjectMapper mapper = new ObjectMapper();
            TypeReference<LinkedHashSet<Pipeline>> typeReference = new TypeReference<>() {};
            try (InputStream inputStream = Files.newInputStream(Paths.get(pipelinesFilePath))) {
                pipelines = mapper.readValue(inputStream, typeReference);
            } catch (IOException e) {
                throw new RepositoryRuntimeException("Error reading pipelines file.", e);
            }
        }
        return pipelines;
    }

    @Override
    public void createPipeline(Pipeline pipeline) {
        getPipelineSet().add(pipeline);
        writePipelinesToFile();
    }

    @Override
    public void updatePipeline(Pipeline pipeline) {
        pipelines.removeIf(p -> p.getId().equals(pipeline.getId()));
        createPipeline(pipeline);
    }

    @Override
    public void deletePipeline(UUID id) {
        pipelines.removeIf(p -> p.getId().equals(id));
        writePipelinesToFile();
    }

    private void writePipelinesToFile() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(pipelines);
            Files.write(Paths.get(pipelinesFilePath), json.getBytes());
        } catch (IOException e) {
            throw new RepositoryRuntimeException("Error writing pipelines file.", e);
        }
    }
}
