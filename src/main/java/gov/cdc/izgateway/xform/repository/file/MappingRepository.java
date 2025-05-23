package gov.cdc.izgateway.xform.repository.file;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.cdc.izgateway.xform.model.Mapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;

@Repository
public class MappingRepository extends GenericFileRepository<Mapping> {

    @Value("${xform.configurations.mappings}")
    protected void setFilePath(String filePath) {
        this.filePath = filePath;
    }


    @Override
    protected TypeReference<LinkedHashSet<Mapping>> getTypeReference() {
        return new TypeReference<>() {};
    }
}
