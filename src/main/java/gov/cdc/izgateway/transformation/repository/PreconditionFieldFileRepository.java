package gov.cdc.izgateway.transformation.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.cdc.izgateway.transformation.model.PreconditionField;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;

@Repository
public class PreconditionFieldFileRepository extends GenericFileRepository<PreconditionField> {

    @Value("${transformationservice.configurations.precondition-fields}")
    protected void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    protected TypeReference<LinkedHashSet<PreconditionField>> getTypeReference() {
        return new TypeReference<>() {};
    }
}

