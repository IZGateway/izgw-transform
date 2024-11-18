package gov.cdc.izgateway.xform.services;

import gov.cdc.izgateway.xform.model.Organization;
import gov.cdc.izgateway.xform.repository.XformRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrganizationService  extends GenericService<Organization> {
    @Autowired
    public OrganizationService(XformRepository<Organization> repo) {
        super(repo);
    }

    public Organization getOrganizationByCommonName(String commonName) {
        return repo.getEntitySet().stream().filter(o -> o.getCommonName().equals(commonName) && Boolean.TRUE.equals(o.getActive())).findFirst().orElse(null);
    }

    public boolean organizationExists(String commonName) {
        return repo.getEntitySet().stream().anyMatch(o -> o.getCommonName().equals(commonName) && Boolean.TRUE.equals(o.getActive()));
    }
}

