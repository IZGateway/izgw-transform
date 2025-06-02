package gov.cdc.izgateway.xform.repository.file;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.cdc.izgateway.xform.model.OperationPreconditionField;

import java.util.LinkedHashSet;

public class OperationPreconditionFieldRepository extends GenericFileRepository<OperationPreconditionField> {

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    protected TypeReference<LinkedHashSet<OperationPreconditionField>> getTypeReference() {
        return new TypeReference<>() {};
    }
}
