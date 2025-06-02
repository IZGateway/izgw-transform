package gov.cdc.izgateway.xform.repository.file;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.cdc.izgateway.xform.model.Organization;

import java.util.LinkedHashSet;

public class OrganizationRepository extends GenericFileRepository<Organization> {

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }


    @Override
    protected TypeReference<LinkedHashSet<Organization>> getTypeReference() {
        return new TypeReference<>() {};
    }
}
