package gov.cdc.izgateway.transformation;
import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cdc.izgateway.transformation.model.*;
import gov.cdc.izgateway.transformation.services.OrganizationService;
import gov.cdc.izgateway.transformation.services.PipelineService;
import gov.cdc.izgateway.transformation.services.SolutionService;
import gov.cdc.izgateway.transformation.services.UserService;
import jakarta.validation.Valid;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;


@Log
@RestController
public class ApiController {
    private final OrganizationService organizationService;
    private final PipelineService pipelineService;
    private final SolutionService solutionService;
    private final UserService userService;

    @Value("${transformationservice.allow-delete-via-api}")
    private Boolean allowDelete;

    @Autowired
    public ApiController(
            OrganizationService organizationService,
            PipelineService pipelineService,
            SolutionService solutionService,
            UserService userService
    ) {
        this.organizationService = organizationService;
        this.pipelineService = pipelineService;
        this.solutionService = solutionService;
        this.userService = userService;
    }

    @GetMapping("/api/v1/pipelines/{uuid}")
    public ResponseEntity<Pipeline> getPipelineByUUID(@PathVariable UUID uuid) {
        Pipeline entity = pipelineService.getObject(uuid);
        if (entity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(entity, HttpStatus.OK);
    }

    @PutMapping("/api/v1/pipelines/{uuid}")
    public ResponseEntity<Pipeline> updatePipeline(@PathVariable UUID uuid, @RequestBody Pipeline updatedPipeline) {
        updatedPipeline.setId(uuid);
        pipelineService.update(updatedPipeline);
        return new ResponseEntity<>(updatedPipeline, HttpStatus.OK);
    }

    @GetMapping("/api/v1/solutions/{uuid}")
    public ResponseEntity<Solution> getSolutionByUUID(@PathVariable UUID uuid) {
        Solution entity = solutionService.getObject(uuid);
        if (entity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(entity, HttpStatus.OK);
    }

    @PutMapping("/api/v1/solutions/{uuid}")
    public ResponseEntity<Solution> updateSolution(@PathVariable UUID uuid, @RequestBody Solution updatedSolution) {
        updatedSolution.setId(uuid);
        solutionService.update(updatedSolution);
        return new ResponseEntity<>(updatedSolution, HttpStatus.OK);
    }

    @GetMapping("/api/v1/organizations/{uuid}")
    public ResponseEntity<Organization> getOrganizationByUUID(@PathVariable UUID uuid) {
        Organization entity = organizationService.getObject(uuid);
        if (entity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(entity, HttpStatus.OK);
    }

    @PutMapping("/api/v1/organizations/{uuid}")
    public ResponseEntity<Organization> updateOrganization(@PathVariable UUID uuid, @RequestBody Organization updatedOrganization) {
        updatedOrganization.setId(uuid);
        organizationService.update(updatedOrganization);
        return new ResponseEntity<>(updatedOrganization, HttpStatus.OK);
    }

    @GetMapping("/api/v1/pipelines")
    public ResponseEntity<String> getPipelinesList(
            @RequestParam(required = false) String nextCursor,
            @RequestParam(required = false) String prevCursor,
            @RequestParam(defaultValue = "false") Boolean includeInactive,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            return processList(pipelineService.getList(), nextCursor, prevCursor, includeInactive, limit);
        } catch (JsonProcessingException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/api/v1/organizations")
    public ResponseEntity<String> getOrganizationsList(
        @RequestParam(required = false) String nextCursor,
        @RequestParam(required = false) String prevCursor,
        @RequestParam(defaultValue = "false") Boolean includeInactive,
        @RequestParam(defaultValue = "10") int limit) {
        try {
            return processList(organizationService.getList(), nextCursor, prevCursor, includeInactive, limit);

        } catch (JsonProcessingException e) { //need to keep this part of the logic because log.log is dependent on @Log
            log.log(Level.SEVERE, e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/api/v1/solutions")
    public ResponseEntity<String> getSolutionsList(
        @RequestParam(required = false) String nextCursor,
        @RequestParam(required = false) String prevCursor,
        @RequestParam(defaultValue = "false") Boolean includeInactive,
        @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            return processList(solutionService.getList(), nextCursor, prevCursor, includeInactive, limit);
        } catch (JsonProcessingException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/api/v1/users")
    public ResponseEntity<String> getUserList(
        @RequestParam(required = false) String nextCursor,
        @RequestParam(required = false) String prevCursor,
        @RequestParam(defaultValue = "false") Boolean includeInactive,
        @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            return processList(userService.getList(), nextCursor, prevCursor, includeInactive, limit);
        } catch (JsonProcessingException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/api/v1/users/{uuid}")
    public ResponseEntity<User> getUserByUUID(@PathVariable UUID uuid) {
        User entity = userService.getObject(uuid);
        if (entity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(entity, HttpStatus.OK);
    }

    @PostMapping("/api/v1/pipelines")
    public ResponseEntity<Pipeline> createPipeline(
            @Valid @RequestBody() Pipeline pipeline
    ) {
        pipelineService.create(pipeline);
        return new ResponseEntity<>(pipeline, HttpStatus.OK);
    }

    @PostMapping("/api/v1/organizations")
    public ResponseEntity<Organization> createOrganization(
            @Valid @RequestBody() Organization organization
             ){
        organizationService.create(organization);
        return new ResponseEntity<>(organization, HttpStatus.OK);
    }

    @PostMapping("/api/v1/solutions")
    public ResponseEntity<Solution> createSolution(
            @Valid @RequestBody() Solution solution
    ) {
        solutionService.create(solution);
        return new ResponseEntity<>(solution, HttpStatus.OK);
    }

    @PostMapping("/api/v1/users")
    public ResponseEntity<User> createUser(
            @Valid @RequestBody() User user
    ) {
        userService.create(user);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @DeleteMapping("/api/v1/pipelines/{uuid}")
    public ResponseEntity<Pipeline> deletePipeline(
            @PathVariable UUID uuid
    ) {

        if (Boolean.FALSE.equals(allowDelete)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        pipelineService.delete(uuid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/api/v1/solutions/{uuid}")
    public ResponseEntity<Solution> deleteSolution(
            @PathVariable UUID uuid
    ) {

        if (Boolean.FALSE.equals(allowDelete)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        solutionService.delete(uuid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/api/v1/organizations/{uuid}")
    public ResponseEntity<Organization> deleteOrganization(
            @PathVariable UUID uuid
    ) {
        if (Boolean.FALSE.equals(allowDelete)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        organizationService.delete(uuid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private <T extends BaseModel> ResponseEntity<String> processList(List<T> allEntityList, String nextCursor, String prevCursor, Boolean includeInactive, int limit) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> returnMap = new HashMap<>();
        int min = 0;
        int max = limit;
        String hasMore = "true";

        List<T> filteredEntityList = filterList(includeInactive, allEntityList);

        for (int i = 0; i < filteredEntityList.size(); i++){
            T newEntity = filteredEntityList.get(i);
            if(newEntity.getId().toString().equals(nextCursor)){
                min = i+1;
                max = i+limit+1;
            }
            else if(newEntity.getId().toString().equals(prevCursor)){
                min = i-limit-1;
                max = i;
            }
        }
        if (max > filteredEntityList.size()) {
            max = filteredEntityList.size();
            hasMore = "false";
        }

        if (min < 0) {
            min = 0;
            hasMore = "false";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        returnMap.put("data", filteredEntityList.subList(min, max));
        returnMap.put("has_more", hasMore);
        return new ResponseEntity<>(mapper.writeValueAsString(returnMap), headers, HttpStatus.OK);
    }

    private <T extends BaseModel> List<T> filterList(Boolean includeInactive, List<T> allList) {
        if (Boolean.FALSE.equals(includeInactive)) {
            return allList.stream()
                    .filter(T::getActive)
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>(allList);
        }
    }

}
