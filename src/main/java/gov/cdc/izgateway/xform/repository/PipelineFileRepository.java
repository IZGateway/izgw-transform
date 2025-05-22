package gov.cdc.izgateway.xform.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.cdc.izgateway.xform.model.Pipeline;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;

@Repository
@ConditionalOnProperty(name = "xform.repository.type", havingValue = "file", matchIfMissing = true)
public class PipelineFileRepository extends GenericFileRepository<Pipeline> {

    @Value("${xform.configurations.pipelines}")
    protected void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    protected TypeReference<LinkedHashSet<Pipeline>> getTypeReference() {
        return new TypeReference<>() {};
    }

}
