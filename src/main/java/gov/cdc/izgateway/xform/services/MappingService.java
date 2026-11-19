package gov.cdc.izgateway.xform.services;

import gov.cdc.izgateway.xform.model.Code;
import gov.cdc.izgateway.xform.model.Mapping;
import gov.cdc.izgateway.xform.repository.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for the code mappings.
 */
@Service
public class MappingService extends GenericService<Mapping> {
    /**
     * Creates the service.
     *
     * @param repositoryFactory the factory that supplies the mapping repository
     */
    @Autowired
    public MappingService(RepositoryFactory repositoryFactory) {
        super(repositoryFactory.mappingRepository());
    }

    /**
     * Finds the mapping for a code in an organization.
     *
     * @param organizationId the id of the organization
     * @param code the code and code system to map
     * @return the mapping, or null if no mapping exists
     */
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
