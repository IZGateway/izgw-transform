package gov.cdc.izgateway.xform.repository.file;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.cdc.izgateway.xform.model.AccessControl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;

@Repository
@ConditionalOnProperty(name = "xform.repository.type", havingValue = "file", matchIfMissing = true)
public class AccessControlRepository extends GenericFileRepository<AccessControl> {

    @Value("${xform.configurations.access-control}")
    protected void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    protected TypeReference<LinkedHashSet<AccessControl>> getTypeReference() {
        return new TypeReference<>() {};
    }
}
