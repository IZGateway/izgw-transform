package gov.cdc.izgateway.transformation.services;

import gov.cdc.izgateway.transformation.model.Pipeline;
import gov.cdc.izgateway.transformation.repository.TxFormRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PipelineService extends GenericService<Pipeline>{
    @Autowired
    public PipelineService(TxFormRepository<Pipeline> repo) {
        super(repo);
    }

}
