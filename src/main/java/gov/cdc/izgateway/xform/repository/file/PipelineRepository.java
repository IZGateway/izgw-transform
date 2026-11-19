package gov.cdc.izgateway.xform.repository.file;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.cdc.izgateway.xform.model.Pipeline;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;

/**
 * File-backed repository for {@link Pipeline} entities.
 */
@Repository
public class PipelineRepository extends GenericFileRepository<Pipeline> {

    /**
     * Creates the file-backed repository.
     */
    public PipelineRepository() {
        // The file path is set by setFilePath.
    }

    /**
     * Sets the path of the pipeline JSON file.
     *
     * @param filePath the path of the JSON file
     */
    @Value("${xform.configurations.pipelines}")
    protected void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    protected TypeReference<LinkedHashSet<Pipeline>> getTypeReference() {
        return new TypeReference<>() {};
    }

}
