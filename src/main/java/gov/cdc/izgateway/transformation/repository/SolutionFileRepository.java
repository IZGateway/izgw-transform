package gov.cdc.izgateway.transformation.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.cdc.izgateway.transformation.model.Solution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;

@Repository
public class SolutionFileRepository extends GenericFileRepository<Solution> {

    @Value("${transformationservice.configurations.solutions}")
    protected void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    protected TypeReference<LinkedHashSet<Solution>> getTypeReference() {
        return new TypeReference<>() {};
    }
}
