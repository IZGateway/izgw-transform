package gov.cdc.izgateway.xform.repository.file;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.cdc.izgateway.xform.model.AccessControl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;

/**
 * File-backed repository for access control entries.
 */
@Repository
public class AccessControlRepository extends GenericFileRepository<AccessControl> {

    /**
     * Create the file-backed repository.
     */
    public AccessControlRepository() {
        super();
    }

    /**
     * Configure the file path for access control data.
     *
     * @param filePath Path to the access control JSON file
     */
    @Value("${xform.configurations.access-control}")
    protected void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Return the type reference for deserializing access control entries.
     *
     * @return Type reference for a linked hash set of access controls
     */
    @Override
    protected TypeReference<LinkedHashSet<AccessControl>> getTypeReference() {
        return new TypeReference<>() {};
    }
}
