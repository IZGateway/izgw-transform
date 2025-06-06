package gov.cdc.izgateway.xform.repository.file;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.cdc.izgateway.xform.model.GroupRoleMapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;

@Repository
public class GroupRoleMappingRepository extends GenericFileRepository<GroupRoleMapping> {

    @Value("${xform.configurations.group-role-mapping}")
    protected void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    protected TypeReference<LinkedHashSet<GroupRoleMapping>> getTypeReference() {
        return new TypeReference<>() {};
    }
}
