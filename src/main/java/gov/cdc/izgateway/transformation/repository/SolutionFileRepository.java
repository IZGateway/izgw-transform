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
public class SolutionFileRepository implements TxFormRepository<Solution> {

    private LinkedHashSet<Solution> solutions;

    @Value("${transformationservice.configurations.solutions}")
    private String solutionFilePath;

    private void writeSolutionsToFile() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(solutions);
            Files.write(Paths.get(solutionFilePath), json.getBytes());
        } catch (IOException e) {
            throw new RepositoryRuntimeException("Error writing solutions file.", e);
        }
    }

    @Override
    public Solution getEntity(UUID id) {
        return getEntitySet().stream().filter(p -> p.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public Set<Solution> getEntitySet() {
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
    public void createEntity(Solution obj) {
        getEntitySet().add(obj);
        writeSolutionsToFile();

    }

    @Override
    public void updateEntity(Solution obj) {
        solutions.removeIf(p -> p.getId().equals(obj.getId()));
        createEntity(obj);
    }

    @Override
    public void deleteEntity(UUID id) {
        solutions.removeIf(p -> p.getId().equals(id));
        writeSolutionsToFile();
    }
}
