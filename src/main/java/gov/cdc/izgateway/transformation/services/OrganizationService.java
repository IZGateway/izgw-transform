package gov.cdc.izgateway.transformation.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cdc.izgateway.transformation.model.Organization;
import gov.cdc.izgateway.transformation.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrganizationService {
    private final OrganizationRepository repo;

    @Autowired
    public OrganizationService(OrganizationRepository repo){
        this.repo = repo;
    }

    public Organization getOrganizationObject(UUID id){
        return repo.getOrganization(id);
    }

    public ResponseEntity<Organization> getOrganizationResponse(UUID id){
        Organization organization = repo.getOrganization(id);

        if ( organization == null ) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(organization, HttpStatus.OK);
    }

    public Set<Organization> getOrganizationSet(){
    return repo.getOrganizationSet();
    }

    public ResponseEntity<String> getOrganizationList(String nextCursor, String prevCursor, Boolean includeInactive, int limit) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> returnMap = new HashMap<>();
        int min = 0, max = limit;
        String hasMore = "true";
        List<Organization> allOrgList = new ArrayList<>(repo.getOrganizationSet());

        List<Organization> filteredOrgList = filterList(includeInactive, allOrgList);
        for (int i = 0; i < filteredOrgList.size(); i++){
            Organization newOrg = filteredOrgList.get(i);
            if(newOrg.getOrganizationId().toString().equals(nextCursor)){
                min = i+1;
                max = i+limit+1;
            }
            else if(newOrg.getOrganizationId().toString().equals(prevCursor)){
                min = i-limit-1;
                max = i;
                }
        }
        if (max > filteredOrgList.size()) {
            max = filteredOrgList.size();
            hasMore = "false";
        }

        if (min < 0) {
            min = 0;
            hasMore = "false";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        returnMap.put("data", filteredOrgList.subList(min, max));
        returnMap.put("has_more", hasMore);
        return new ResponseEntity<>(mapper.writeValueAsString(returnMap), headers, HttpStatus.OK);
    }

    private static List<Organization> filterList(Boolean includeInactive, List<Organization> allOrgList) {
        if (Boolean.FALSE.equals(includeInactive)) {
            return allOrgList.stream()
                    .filter(Organization::getActive)
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>(allOrgList);
        }
    }

    public void updateOrganization(Organization organization) {
        Organization existingOrganization = getOrganizationObject(organization.getOrganizationId());
        if (existingOrganization == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found");
        }

        repo.updateOrganization(organization);

    }

    public Organization createOrganization(String orgName){
        Organization newOrg = new Organization(UUID.randomUUID(), orgName, true);
        repo.createOrganization(newOrg);
        return newOrg;
    }
}
