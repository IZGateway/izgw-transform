package gov.cdc.izgateway.transformation.services;

import gov.cdc.izgateway.transformation.model.Solution;
import gov.cdc.izgateway.transformation.repository.TxFormRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SolutionService extends GenericService<Solution> {
    @Autowired
    public SolutionService(TxFormRepository<Solution> repo) {
        super(repo);
    }

}
