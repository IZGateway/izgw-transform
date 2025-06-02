package gov.cdc.izgateway.xform.repository.file;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.cdc.izgateway.xform.model.Mapping;

import java.util.LinkedHashSet;

public class MappingRepository extends GenericFileRepository<Mapping> {

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }


    @Override
    protected TypeReference<LinkedHashSet<Mapping>> getTypeReference() {
        return new TypeReference<>() {};
    }
}
