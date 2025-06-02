package gov.cdc.izgateway.xform.repository.file;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.cdc.izgateway.xform.model.GroupRoleMapping;

import java.util.LinkedHashSet;

public class GroupRoleMappingRepository extends GenericFileRepository<GroupRoleMapping> {

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    protected TypeReference<LinkedHashSet<GroupRoleMapping>> getTypeReference() {
        return new TypeReference<>() {};
    }
}
