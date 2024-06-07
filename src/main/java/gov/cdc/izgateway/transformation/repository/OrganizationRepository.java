package gov.cdc.izgateway.transformation.repository;

import gov.cdc.izgateway.transformation.configuration.OrganizationConfig;
import gov.cdc.izgateway.transformation.model.Organization;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface OrganizationRepository{
    public Organization getOrganization(UUID id);
    public void createOrganization(Organization org);
    public void saveOrganization(Organization org);
}
