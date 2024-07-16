package gov.cdc.izgateway.transformation.services;

import gov.cdc.izgateway.transformation.model.Organization;
import gov.cdc.izgateway.transformation.repository.TxFormRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrganizationService  extends GenericService<Organization> {
    @Autowired
    public OrganizationService(TxFormRepository<Organization> repo) {
        super(repo);
    }
}

