package gov.cdc.izgateway.xform.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.cdc.izgateway.xform.model.AccessControl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;

@Repository
public class AccessControlFileRepository extends GenericFileRepository<AccessControl> {

    @Value("${xform.configurations.access-control}")
    protected void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    protected TypeReference<LinkedHashSet<AccessControl>> getTypeReference() {
        return new TypeReference<>() {};
    }
}
