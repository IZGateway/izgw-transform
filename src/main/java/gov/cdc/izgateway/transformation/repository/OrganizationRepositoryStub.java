package gov.cdc.izgateway.transformation.repository;

import gov.cdc.izgateway.transformation.model.Organization;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class OrganizationRepositoryStub implements OrganizationRepository{

    @Override
    public Organization getOrganization(UUID id) {
        Organization org = new Organization();
        org.setActive(true);
        org.setOrganizationId(new UUID(0, 0));
        org.setOrganizationName("test org 21");
        return(org);
    }

    @Override
    public void createOrganization(Organization org) {

    }

    @Override
    public void saveOrganization(Organization org) {

    }
}
