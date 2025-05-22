package gov.cdc.izgateway.xform.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.cdc.izgateway.xform.model.Organization;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;

@Repository
@ConditionalOnProperty(name = "xform.repository.type", havingValue = "file", matchIfMissing = true)
@Primary
public class OrganizationFileRepository extends GenericFileRepository<Organization> {

    @Value("${xform.configurations.organizations}")
    protected void setFilePath(String filePath) {
        this.filePath = filePath;
    }


    @Override
    protected TypeReference<LinkedHashSet<Organization>> getTypeReference() {
        return new TypeReference<>() {};
    }
}
