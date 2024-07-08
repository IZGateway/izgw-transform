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

    @Value("${transformationservice.configurations.organizations}")
    private String organizationFilePath ;

    @Override
    public Pipeline getPipeline(UUID id) {
        return null;
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
}
