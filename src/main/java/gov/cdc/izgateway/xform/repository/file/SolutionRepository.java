package gov.cdc.izgateway.xform.repository.file;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.cdc.izgateway.xform.model.Solution;

import java.util.LinkedHashSet;

public class SolutionRepository extends GenericFileRepository<Solution> {

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    protected TypeReference<LinkedHashSet<Solution>> getTypeReference() {
        return new TypeReference<>() {};
    }
}
