package gov.cdc.izgateway.xform.repository.file;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.cdc.izgateway.xform.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;

@Repository
public class UserRepository extends GenericFileRepository<User> {

    @Value("${xform.configurations.users}")
    protected void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    protected TypeReference<LinkedHashSet<User>> getTypeReference() {
        return new TypeReference<>() {};
    }
}
