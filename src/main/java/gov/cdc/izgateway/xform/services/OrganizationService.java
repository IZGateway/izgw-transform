package gov.cdc.izgateway.xform.services;

import gov.cdc.izgateway.xform.model.Organization;
import gov.cdc.izgateway.xform.repository.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrganizationService  extends GenericService<Organization> {
    @Autowired
    public OrganizationService(RepositoryFactory repositoryFactory) {
        super(repositoryFactory.organizationRepository());
    }

    public Organization getOrganizationByCommonName(String commonName) {
        return repo.getEntitySet().stream().filter(o -> o.getCommonName().equals(commonName) && Boolean.TRUE.equals(o.getActive())).findFirst().orElse(null);
    }

    public boolean organizationExists(String commonName) {
        return repo.getEntitySet().stream().anyMatch(o -> o.getCommonName().equals(commonName) && Boolean.TRUE.equals(o.getActive()));
    }

    /**
     * Checks if an organization with the same organizationName and commonName already exists.
     *
     * @param organization The organization to check for duplication
     * @return true if a duplicate exists, false otherwise
     */
    @Override
    protected boolean isDuplicate(Organization organization) {
        return repo.getEntitySet().stream()
                .anyMatch(o ->
                        o.getOrganizationName().equalsIgnoreCase(organization.getOrganizationName()) &&
                                o.getCommonName().equalsIgnoreCase(organization.getCommonName())
                );
    }
}
