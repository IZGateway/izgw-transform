package gov.cdc.izgateway.transformation.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cdc.izgateway.transformation.model.BaseModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

public class TxFormRepositoryUtils {

    private TxFormRepositoryUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static <T extends BaseModel> void writeEntitiesToFile(String filePath, Set<T> entities) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(entities);
            Files.write(Paths.get(filePath), json.getBytes());
        } catch (IOException e) {
            throw new RepositoryRuntimeException("Error writing file.", e);
        }
    }

}
