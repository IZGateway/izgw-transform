package gov.cdc.izgateway.xform.services;

import gov.cdc.izgateway.xform.model.Code;
import gov.cdc.izgateway.xform.model.Mapping;
import gov.cdc.izgateway.xform.repository.XformRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MappingService extends GenericService<Mapping> {
    @Autowired
    public MappingService(XformRepository<Mapping> repo) {
        super(repo);
    }

    public Mapping getMapping(UUID organizationId, Code code) {
        return repo.getEntitySet().stream()
                .filter(m -> m.getOrganizationId().equals(organizationId.toString()) && m.getCodeSystem().equals(code.codeSystem()) && m.getCode().equals(code.code()))
                .findFirst()
                .orElse(null);
    }
}

