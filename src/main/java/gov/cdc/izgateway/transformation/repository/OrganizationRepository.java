package gov.cdc.izgateway.transformation.repository;

import gov.cdc.izgateway.transformation.model.Organization;

import java.util.UUID;

public interface OrganizationRepository {
    public Organization getOrganization(UUID id);
    public void createOrganization(Organization org);
    public void updateOrganization(Organization org);
}
