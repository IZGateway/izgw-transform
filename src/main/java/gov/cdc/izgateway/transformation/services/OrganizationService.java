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
        List<Organization> orgList = new ArrayList<>();
        String hasMore = "true";
        List<Organization> allOrgList = new ArrayList<>(repo.getOrganizationSet());

        getActiveList(includeInactive, allOrgList, orgList);
        for (int i = 0; i < orgList.size(); i++){
            Organization newOrg = orgList.get(i);
            if(newOrg.getOrganizationId().toString().equals(nextCursor)){
                min = i+1;
                max = i+limit+1;
            }
            else if(newOrg.getOrganizationId().toString().equals(prevCursor)){
                min = i-limit-1;
                max = i;
                }
        }
        if (max > orgList.size()) {
            max = orgList.size();
            hasMore = "false";
        }

        if (min < 0) {

            min = 0;
            hasMore = "false";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        returnMap.put("data", orgList.subList(min, max));
        returnMap.put("has_more", hasMore);
        return new ResponseEntity<>(mapper.writeValueAsString(returnMap), headers, HttpStatus.OK);
    }

    private static void getActiveList(Boolean includeInactive, List<Organization> allOrgList, List<Organization> orgList) {
        for (Organization newOrg : allOrgList) {
            if (Boolean.FALSE.equals(includeInactive)) {
                if (Boolean.TRUE.equals(newOrg.getActive())) {
                    orgList.add(newOrg);
                }
            } else {
                orgList.add(newOrg);
            }
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
