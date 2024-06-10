package gov.cdc.izgateway.transformation.services;

import gov.cdc.izgateway.transformation.model.Organization;
import gov.cdc.izgateway.transformation.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class OrganizationService {
    private final OrganizationRepository repo;

    @Autowired
    public OrganizationService(OrganizationRepository repo){
        this.repo = repo;
    }

    public Organization getOrganization(UUID id){
        return repo.getOrganization(id);
    }

    public void updateOrganization(Organization organization) {
        Organization existingOrganization = getOrganization(organization.getOrganizationId());
        if (existingOrganization == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found");
        }

        repo.updateOrganization(organization);

    }
}
