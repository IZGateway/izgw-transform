package gov.cdc.izgateway.transformation.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cdc.izgateway.transformation.model.BaseModel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public abstract class GenericFileRepository<T extends BaseModel> implements TxFormRepository<T> {


    protected LinkedHashSet<T> entities;
    protected String filePath;
    protected abstract TypeReference<LinkedHashSet<T>> getTypeReference();

    @Override
    public T getEntity(UUID id) {
        return getEntitySet().stream().filter(p -> p.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public Set<T> getEntitySet() {
        if (entities == null) {
            ObjectMapper mapper = new ObjectMapper();
            try (InputStream inputStream = Files.newInputStream(Paths.get(filePath))) {
                entities = mapper.readValue(inputStream, getTypeReference());
            } catch (IOException e) {
                throw new RepositoryRuntimeException("Error reading pipelines file.", e);
            }
        }
        return entities;
    }

    @Override
    public void createEntity(T obj) {
        getEntitySet().add(obj);
        writeEntitiesToFile();
    }

    @Override
    public void updateEntity(T obj) {
        entities.removeIf(p -> p.getId().equals(obj.getId()));
        createEntity(obj);
    }

    @Override
    public void deleteEntity(UUID id) {
        entities.removeIf(p -> p.getId().equals(id));
        writeEntitiesToFile();
    }

    private void writeEntitiesToFile() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(entities);
            Files.write(Paths.get(filePath), json.getBytes());
        } catch (IOException e) {
            throw new RepositoryRuntimeException("Error writing file.", e);
        }
    }
}
