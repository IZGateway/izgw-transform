package gov.cdc.izgateway.transformation.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.cdc.izgateway.transformation.model.Pipeline;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;
import java.util.UUID;


@Repository
public class PipelineFileRepository extends GenericFileRepository<Pipeline> {

    @Value("${transformationservice.configurations.pipelines}")
    protected void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    protected TypeReference<LinkedHashSet<Pipeline>> getTypeReference() {
        return new TypeReference<>() {};
    }

}
