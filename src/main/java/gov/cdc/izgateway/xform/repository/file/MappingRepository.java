package gov.cdc.izgateway.xform.repository.file;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.cdc.izgateway.xform.model.Mapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;

/**
 * File-backed repository for {@link Mapping} entities.
 */
@Repository
public class MappingRepository extends GenericFileRepository<Mapping> {

    /**
     * Creates the file-backed repository.
     */
    public MappingRepository() {
        // The file path is set by setFilePath.
    }

    /**
     * Sets the path of the mapping JSON file.
     *
     * @param filePath the path of the JSON file
     */
    @Value("${xform.configurations.mappings}")
    protected void setFilePath(String filePath) {
        this.filePath = filePath;
    }


    @Override
    protected TypeReference<LinkedHashSet<Mapping>> getTypeReference() {
        return new TypeReference<>() {};
    }
}
