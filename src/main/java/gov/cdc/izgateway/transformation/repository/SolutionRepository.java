package gov.cdc.izgateway.transformation.repository;

import gov.cdc.izgateway.transformation.model.Solution;

import java.util.Set;
import java.util.UUID;

public interface SolutionRepository {
    public Solution getSolution(UUID id);
    public Set<Solution> getSolutionSet();
    public void createSolution(Solution solution);
    public void updateSolution(Solution solution);
    public void deleteSolution(UUID id);

}
