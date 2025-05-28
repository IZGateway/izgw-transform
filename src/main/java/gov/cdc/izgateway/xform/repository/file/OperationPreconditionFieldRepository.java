package gov.cdc.izgateway.xform.repository.file;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.cdc.izgateway.xform.model.OperationPreconditionField;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;

@ConditionalOnProperty(name = "xform.repository.type", havingValue = "file", matchIfMissing = true)
@Repository
public class OperationPreconditionFieldRepository extends GenericFileRepository<OperationPreconditionField> {

    @Value("${xform.configurations.operation-precondition-fields}")
    protected void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    protected TypeReference<LinkedHashSet<OperationPreconditionField>> getTypeReference() {
        return new TypeReference<>() {};
    }
}
