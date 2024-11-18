package gov.cdc.izgateway.xform.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.cdc.izgateway.xform.model.OperationPreconditionField;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;

@Repository
public class OperationPreconditionFieldFileRepository extends GenericFileRepository<OperationPreconditionField> {

    @Value("${transformationservice.configurations.operation-precondition-fields}")
    protected void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    protected TypeReference<LinkedHashSet<OperationPreconditionField>> getTypeReference() {
        return new TypeReference<>() {};
    }
}
