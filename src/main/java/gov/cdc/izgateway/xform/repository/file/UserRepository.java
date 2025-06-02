package gov.cdc.izgateway.xform.repository.file;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.cdc.izgateway.xform.model.User;

import java.util.LinkedHashSet;

public class UserRepository extends GenericFileRepository<User> {

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    protected TypeReference<LinkedHashSet<User>> getTypeReference() {
        return new TypeReference<>() {};
    }
}
