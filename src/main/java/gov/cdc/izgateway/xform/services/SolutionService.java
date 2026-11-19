package gov.cdc.izgateway.xform.services;

import gov.cdc.izgateway.xform.model.Solution;
import gov.cdc.izgateway.xform.repository.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for the solutions.
 */
@Service
public class SolutionService extends GenericService<Solution> {
    /**
     * Creates the service.
     *
     * @param repositoryFactory the factory that supplies the solution repository
     */
    @Autowired
    public SolutionService(RepositoryFactory repositoryFactory) {
        super(repositoryFactory.solutionRepository());
    }

    @Override
    protected boolean isDuplicate(Solution solution) {
        return repo.getEntitySet().stream().anyMatch(s ->
                s.getSolutionName().equalsIgnoreCase(solution.getSolutionName()) &&
                        s.getVersion().equalsIgnoreCase(solution.getVersion())
        );
    }

}
