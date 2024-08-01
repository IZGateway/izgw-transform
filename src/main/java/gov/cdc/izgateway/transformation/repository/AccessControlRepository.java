package gov.cdc.izgateway.transformation.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.cdc.izgateway.transformation.model.Solution;
import gov.cdc.izgateway.transformation.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;

@Repository
public class AccessControlRepository extends GenericFileRepository<User>{
    @Value("${transformationservice.configurations.users}")
    protected void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    protected TypeReference<LinkedHashSet<User>> getTypeReference() {
        return new TypeReference<>() {};
    }
}
