package gov.cdc.izgateway.xform.services;

import gov.cdc.izgateway.xform.model.Organization;
import gov.cdc.izgateway.xform.repository.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for the organizations.
 */
@Service
public class OrganizationService  extends GenericService<Organization> {
    /**
     * Creates the service.
     *
     * @param repositoryFactory the factory that supplies the organization repository
     */
    @Autowired
    public OrganizationService(RepositoryFactory repositoryFactory) {
        super(repositoryFactory.organizationRepository());
    }

    /**
     * Finds the active organization with a common name.
     *
     * @param commonName the common name
     * @return the organization, or null if no active organization has the common name
     */
    public Organization getOrganizationByCommonName(String commonName) {
        return repo.getEntitySet().stream().filter(o -> o.getCommonName().equals(commonName) && Boolean.TRUE.equals(o.getActive())).findFirst().orElse(null);
    }

    /**
     * Checks if an active organization with a common name exists.
     *
     * @param commonName the common name
     * @return true if an active organization has the common name
     */
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
