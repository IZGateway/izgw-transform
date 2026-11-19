package gov.cdc.izgateway.xform.repository.file;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.cdc.izgateway.xform.model.Solution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;

/**
 * File-backed repository for {@link Solution} entities.
 */
@Repository
public class SolutionRepository extends GenericFileRepository<Solution> {

    /**
     * Creates the file-backed repository.
     */
    public SolutionRepository() {
        // The file path is set by setFilePath.
    }

    /**
     * Sets the path of the solution JSON file.
     *
     * @param filePath the path of the JSON file
     */
    @Value("${xform.configurations.solutions}")
    protected void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    protected TypeReference<LinkedHashSet<Solution>> getTypeReference() {
        return new TypeReference<>() {};
    }
}
