package gov.cdc.izgateway.xform.repository.file;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.cdc.izgateway.xform.model.Organization;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;

/**
 * File-backed repository for {@link Organization} entities.
 */
@Repository
public class OrganizationRepository extends GenericFileRepository<Organization> {

    /**
     * Creates the file-backed repository.
     */
    public OrganizationRepository() {
        // The file path is set by setFilePath.
    }

    /**
     * Sets the path of the organization JSON file.
     *
     * @param filePath the path of the JSON file
     */
    @Value("${xform.configurations.organizations}")
    protected void setFilePath(String filePath) {
        this.filePath = filePath;
    }


    @Override
    protected TypeReference<LinkedHashSet<Organization>> getTypeReference() {
        return new TypeReference<>() {};
    }
}
