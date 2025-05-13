package gov.cdc.izgateway.xform.services;

import gov.cdc.izgateway.xform.model.Solution;
import gov.cdc.izgateway.xform.repository.XformRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SolutionService extends GenericService<Solution> {
    @Autowired
    public SolutionService(XformRepository<Solution> repo) {
        super(repo);
    }

    @Override
    protected boolean isDuplicate(Solution solution) {
        return repo.getEntitySet().stream().anyMatch(s ->
                        s.getSolutionName().equalsIgnoreCase(solution.getSolutionName()) &&
                        s.getVersion().equalsIgnoreCase(solution.getVersion())
        );
    }

}
