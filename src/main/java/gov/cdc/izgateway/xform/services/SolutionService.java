package gov.cdc.izgateway.xform.services;

import gov.cdc.izgateway.xform.model.Solution;
import gov.cdc.izgateway.xform.repository.TxFormRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SolutionService extends GenericService<Solution> {
    @Autowired
    public SolutionService(TxFormRepository<Solution> repo) {
        super(repo);
    }

}
