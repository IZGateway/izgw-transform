package gov.cdc.izgateway.transformation.services;

import gov.cdc.izgateway.transformation.model.Organization;
import gov.cdc.izgateway.transformation.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}

