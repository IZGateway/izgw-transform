package gov.cdc.izgateway.xform.repository.file;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.cdc.izgateway.xform.model.AccessControl;

import java.util.LinkedHashSet;

public class AccessControlRepository extends GenericFileRepository<AccessControl> {

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    protected TypeReference<LinkedHashSet<AccessControl>> getTypeReference() {
        return new TypeReference<>() {};
    }
}
