package gov.cdc.izgateway.transformation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.cdc.izgateway.transformation.configuration.ServiceConfig;
import gov.cdc.izgateway.transformation.model.Organization;
import gov.cdc.izgateway.transformation.services.OrganizationService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.logging.Level;

import org.json.simple.parser.JSONParser;


@Log
@RestController
public class ApiController {
    private final ServiceConfig configuration;
    private final OrganizationService organizationService;

    @Autowired
    public ApiController(ServiceConfig configuration, OrganizationService organizationService) {
        this.configuration = configuration;
        this.organizationService = organizationService;
    }

    @GetMapping("/api/v1/organizations/{uuid}")
    public ResponseEntity<Organization> getOrganizationByUUID(@PathVariable UUID uuid) {
        Organization organization = organizationService.getOrganization(uuid);

        if ( organization == null ) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(organization, HttpStatus.OK);
    }

    @PutMapping("/api/v1/organizations/{uuid}")
    public ResponseEntity<Organization> updateOrganization(@PathVariable UUID uuid, @RequestBody Map<String, Object> updatedOrganization) {
        Organization newOrg = new Organization(UUID.fromString(updatedOrganization.get("organizationId").toString()), updatedOrganization.get("organizationName").toString(), (Boolean) updatedOrganization.get("active"));
        organizationService.updateOrganization(newOrg);
        return new ResponseEntity<>(newOrg, HttpStatus.OK);
    }

    @GetMapping("/api/v1/organizations")
    @ResponseBody
    public ResponseEntity<String> getOrganizationsList(
        @RequestParam(required = false) String next_cursor,
        @RequestParam(required = false) String prev_cursor,
        @RequestParam(defaultValue = "false") Boolean include_inactive,
        @RequestParam(defaultValue = "10") int limit) {

        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, Object> returnMap = new HashMap<String, Object>();
            int min = 0, max = limit;
            List<Organization> orgList = new ArrayList<Organization>();
            String hasMore = "true";
            List<Organization> allOrgList = new ArrayList<Organization> (organizationService.getOrganizationSet());
            //Iterator<Organization> orgIterator= organizationService.getOrganizationSet().iterator();

            for (Organization newOrg : allOrgList) {
                if (!include_inactive) {
                    if (newOrg.getActive()) {
                        orgList.add(newOrg);
                    }
                } else {
                    orgList.add(newOrg);
                }
            }
            for (int i = 0; i < orgList.size(); i++){
                Organization newOrg = orgList.get(i);
                if(next_cursor != null){
                    if(newOrg.getOrganizationId().toString().equals(next_cursor)){
                        min = i+1;
                        max = i+limit+1;
                    }
                }
                else if(prev_cursor != null){
                    if(newOrg.getOrganizationId().toString().equals(prev_cursor)){
                        min = i-limit-1;
                        max = i;
                    }
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

        } catch (JsonProcessingException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/v1/organizations")
    @ResponseBody
    public ResponseEntity<Organization> createOrganization(
            @RequestBody() String orgName
             ){
            JSONParser jsonParser = new JSONParser();
            Organization newOrg = new Organization(UUID.randomUUID(), orgName, true);

            organizationService.createOrganization(newOrg);
            return new ResponseEntity<>(newOrg, HttpStatus.OK);
    }
}
