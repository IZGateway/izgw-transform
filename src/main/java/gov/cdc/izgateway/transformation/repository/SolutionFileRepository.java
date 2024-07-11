package gov.cdc.izgateway.transformation.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cdc.izgateway.transformation.model.Solution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Repository
public class SolutionFileRepository implements SolutionRepository {

    private LinkedHashSet<Solution> solutions;

    @Value("${transformationservice.configurations.solutions}")
    private String solutionFilePath;

    @Override
    public Solution getSolution(UUID id) {
        return getSolutionSet().stream().filter(p -> p.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public Set<Solution> getSolutionSet() {
        if (solutions == null) {
            ObjectMapper mapper = new ObjectMapper();
            TypeReference<LinkedHashSet<Solution>> typeReference = new TypeReference<>() {};
            try (InputStream inputStream = Files.newInputStream(Paths.get(solutionFilePath))) {
                solutions = mapper.readValue(inputStream, typeReference);
            } catch (IOException e) {
                throw new RepositoryRuntimeException("Error reading pipelines file.", e);
            }
        }
        return solutions;
    }

    @Override
    public void createSolution(Solution solution) {

    }

    @Override
    public void updateSolution(Solution solution) {

    }

    @Override
    public void deleteSolution(UUID id) {

    }
}
