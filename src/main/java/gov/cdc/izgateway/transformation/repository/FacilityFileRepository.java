package gov.cdc.izgateway.transformation.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.cdc.izgateway.transformation.model.Facility;
import gov.cdc.izgateway.transformation.model.Organization;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;

@Repository
public class FacilityFileRepository  extends GenericFileRepository<Facility> {

    @Value("${transformationservice.configurations.facilities}")
    protected void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    protected TypeReference<LinkedHashSet<Facility>> getTypeReference() {
        return new TypeReference<>() {};
    }}
