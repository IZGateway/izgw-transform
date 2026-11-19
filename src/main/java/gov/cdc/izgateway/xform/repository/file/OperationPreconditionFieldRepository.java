package gov.cdc.izgateway.xform.repository.file;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.cdc.izgateway.xform.model.OperationPreconditionField;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;

/**
 * File-backed repository for {@link OperationPreconditionField} entities.
 */
@Repository
public class OperationPreconditionFieldRepository extends GenericFileRepository<OperationPreconditionField> {

    /**
     * Creates the file-backed repository.
     */
    public OperationPreconditionFieldRepository() {
        // The file path is set by setFilePath.
    }

    /**
     * Sets the path of the operation precondition field JSON file.
     *
     * @param filePath the path of the JSON file
     */
    @Value("${xform.configurations.operation-precondition-fields}")
    protected void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    protected TypeReference<LinkedHashSet<OperationPreconditionField>> getTypeReference() {
        return new TypeReference<>() {};
    }
}
