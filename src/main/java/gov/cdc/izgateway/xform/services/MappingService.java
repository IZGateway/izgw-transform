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
                .filter(m -> m.getOrganizationId().equals(organizationId) && m.getCodeSystem().equals(code.codeSystem()) && m.getCode().equals(code.code()))
                .findFirst()
                .orElse(null);
    }

    @Override
    protected boolean isDuplicate(Mapping mapping) {
        return repo.getEntitySet().stream()
                .anyMatch(m ->
                        m.getOrganizationId().equals(mapping.getOrganizationId()) &&
                                m.getCodeSystem().equalsIgnoreCase(mapping.getCodeSystem()) &&
                                m.getCode().equalsIgnoreCase(mapping.getCode()) &&
                                m.getTargetCodeSystem().equalsIgnoreCase(mapping.getTargetCodeSystem()) &&
                                m.getTargetCode().equalsIgnoreCase(mapping.getTargetCode())
                );
    }

}

