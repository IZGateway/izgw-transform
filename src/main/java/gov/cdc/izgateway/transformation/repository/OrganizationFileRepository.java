package gov.cdc.izgateway.transformation.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import gov.cdc.izgateway.transformation.model.Organization;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Repository
public class OrganizationFileRepository implements OrganizationRepository {
    private LinkedHashSet<Organization> organizations;
    @Value("${transformation.organization-file-path:organizations.json}")
    private String organizationFilePath ;

    @Override
    public Organization getOrganization(UUID id) {
        return getOrganizationSet().stream()
                .filter(org -> org.getOrganizationId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void createOrganization(Organization org) {
        getOrganizationSet().add(org);
        writeOrganizationsToFile();
    }

    @Override
    public void updateOrganization(Organization org) {
        organizations.removeIf(o -> o.getOrganizationId().equals(org.getOrganizationId()));
        organizations.add(org);
        writeOrganizationsToFile();
    }

    @Override
    public LinkedHashSet<Organization> getOrganizationSet() {
        if (organizations == null) {
            ObjectMapper mapper = new ObjectMapper();
            TypeReference<LinkedHashSet<Organization>> typeReference = new TypeReference<>() {};
            try (InputStream inputStream = Files.newInputStream(Paths.get(organizationFilePath))) {
                organizations = mapper.readValue(inputStream, typeReference);
            } catch (IOException e) {
                throw new RepositoryRuntimeException("Error reading organizations file.", e);
            }
        }
        return organizations;
    }

    private void writeOrganizationsToFile() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(organizations);
            Files.write(Paths.get(organizationFilePath), json.getBytes());
        } catch (IOException e) {
            throw new RepositoryRuntimeException("Error writing organizations file.", e);
        }
    }
}
