package gov.cdc.izgateway.transformation.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.cdc.izgateway.transformation.model.Mapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;

@Repository
public class MappingFileRepository extends GenericFileRepository<Mapping> {

    @Value("${transformationservice.configurations.mappings}")
    protected void setFilePath(String filePath) {
        this.filePath = filePath;
    }


    @Override
    protected TypeReference<LinkedHashSet<Mapping>> getTypeReference() {
        return new TypeReference<>() {};
    }
}
