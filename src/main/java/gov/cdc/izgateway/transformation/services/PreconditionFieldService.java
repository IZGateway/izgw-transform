package gov.cdc.izgateway.transformation.services;

import gov.cdc.izgateway.transformation.model.PreconditionField;
import gov.cdc.izgateway.transformation.repository.TxFormRepository;
import org.springframework.stereotype.Service;

@Service
public class PreconditionFieldService extends GenericService<PreconditionField> {

    protected PreconditionFieldService(TxFormRepository<PreconditionField> repo) {
        super(repo);
    }
}
