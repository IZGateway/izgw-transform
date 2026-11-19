package gov.cdc.izgateway.xform.repository.file;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.cdc.izgateway.xform.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;

/**
 * File-backed repository for {@link User} entities.
 */
@Repository
public class UserRepository extends GenericFileRepository<User> {

    /**
     * Creates the file-backed repository.
     */
    public UserRepository() {
        // The file path is set by setFilePath.
    }

    /**
     * Sets the path of the user JSON file.
     *
     * @param filePath the path of the JSON file
     */
    @Value("${xform.configurations.users}")
    protected void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    protected TypeReference<LinkedHashSet<User>> getTypeReference() {
        return new TypeReference<>() {};
    }
}
