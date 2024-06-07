package gov.cdc.izgateway.transformation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.cdc.izgateway.transformation.configuration.OrganizationConfig;
import gov.cdc.izgateway.transformation.configuration.ServiceConfig;
import gov.cdc.izgateway.transformation.model.Organization;
import gov.cdc.izgateway.transformation.repository.OrganizationRepository;
import gov.cdc.izgateway.transformation.services.OrganizationService;
import lombok.extern.java.Log;
//import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.logging.Level;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;

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

    @Autowired
    private OrganizationRepository repo;

    @GetMapping("/api/v1/organizations")
    @ResponseBody
    public ResponseEntity<String> getOrganizationsList(
        @RequestParam(defaultValue = "25") int limit,
        @RequestParam(required = false) String next_cursor,
        @RequestParam(required = false) String prev_cursor,
        @RequestParam(defaultValue = "false") Boolean inlcude_inactive) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, Object> returnMap = new HashMap<String, Object>();
            int min = 0, max = limit;
            List<OrganizationConfig> orgList = new ArrayList<OrganizationConfig>();
            String hasMore = "true";
            for (int i = 0; i < configuration.getOrganizations().size(); i++){
                OrganizationConfig newOrg = configuration.getOrganizations().get(i);
                if(!inlcude_inactive){
                    if(newOrg.getActive()){
                        orgList.add(newOrg);
                    }
                }
                else{
                    orgList.add(newOrg);
                }
            }
            for (int i = 0; i < orgList.size(); i++){
                OrganizationConfig newOrg = orgList.get(i);
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
    @GetMapping("/api/v1/organizations/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}}")
    @ResponseBody
    public ResponseEntity<String> getOrganizationById(@PathVariable UUID id){
        try {
            ObjectMapper mapper = new ObjectMapper();
            HttpHeaders headers = new HttpHeaders();
            Organization newOrg = organizationService.getOrganization(id);
            headers.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity<>(mapper.writeValueAsString(newOrg), headers, HttpStatus.OK);

        } catch (JsonProcessingException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/v1/organizations")
    @ResponseBody
    public ResponseEntity<String> createOrganization(
            @RequestBody String body,
            @RequestParam() int uuid,
            @RequestParam() String orgName,
            @RequestParam(defaultValue = "") String[] pipelines,
            @RequestParam(defaultValue = "true") Boolean active){
        try {
            ObjectMapper mapper = new ObjectMapper();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            JSONParser jsonParser = new JSONParser();
            Object obj = jsonParser.parse(new FileReader("src\\main\\resources\\pipelines_test_preconditions.json"));
            JSONObject json = (JSONObject)obj;
            JSONArray orgArray = (JSONArray) json.get("organizations");
            JSONArray solutions = (JSONArray) json.get("solutions");
            JSONObject orgs = new JSONObject();
            JSONObject returnJson = new JSONObject();
            orgs.put("organizationId", uuid);
            orgs.put("pipelines", pipelines);
            orgs.put("organizationName", orgName);
            orgs.put("active", active);
            orgArray.add(orgs);
            returnJson.put("organizations", orgArray);
            returnJson.put("solutions", solutions);
            FileWriter file = new FileWriter("src\\main\\resources\\pipelines_test_preconditions.json");
            file.write(mapper.writeValueAsString(returnJson));
            file.flush();
            file.close();

            //repo.save(returnJson);
            return new ResponseEntity<>(mapper.writeValueAsString("test"), headers, HttpStatus.OK);
        } catch (ParseException | IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
