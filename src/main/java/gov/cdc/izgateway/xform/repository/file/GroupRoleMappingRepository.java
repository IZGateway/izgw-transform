package gov.cdc.izgateway.xform.repository.file;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.cdc.izgateway.xform.model.GroupRoleMapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;

/**
 * File-backed repository for {@link GroupRoleMapping} entities.
 */
@Repository
public class GroupRoleMappingRepository extends GenericFileRepository<GroupRoleMapping> {

    /**
     * Creates the file-backed repository.
     */
    public GroupRoleMappingRepository() {
        // The file path is set by setFilePath.
    }

    /**
     * Sets the path of the group role mapping JSON file.
     *
     * @param filePath the path of the JSON file
     */
    @Value("${xform.configurations.group-role-mapping}")
    protected void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    protected TypeReference<LinkedHashSet<GroupRoleMapping>> getTypeReference() {
        return new TypeReference<>() {};
    }
}
