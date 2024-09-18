package gov.cdc.izgateway.transformation.services;

import gov.cdc.izgateway.transformation.model.Code;
import gov.cdc.izgateway.transformation.model.Mapping;
import gov.cdc.izgateway.transformation.model.Organization;
import gov.cdc.izgateway.transformation.repository.TxFormRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MappingService extends GenericService<Mapping> {
    @Autowired
    public MappingService(TxFormRepository<Mapping> repo) {
        super(repo);
    }

    public Mapping getMapping(UUID organizationId, Code code) {
        return repo.getEntitySet().stream()
                .filter(m -> m.getOrganizationId().equals(organizationId.toString()) && m.getCodeSystem().equals(code.codeSystem()) && m.getCode().equals(code.code()))
                .findFirst()
                .orElse(null);
    }
}

