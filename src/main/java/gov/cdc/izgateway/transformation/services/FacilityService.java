package gov.cdc.izgateway.transformation.services;

import gov.cdc.izgateway.transformation.model.Facility;
import gov.cdc.izgateway.transformation.repository.TxFormRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FacilityService extends GenericService<Facility> {
    @Autowired
    public FacilityService(TxFormRepository<Facility> repo) {
        super(repo);
    }
}
