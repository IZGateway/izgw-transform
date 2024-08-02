package gov.cdc.izgateway.transformation;
import com.fasterxml.jackson.core.JsonProcessingException;

import gov.cdc.izgateway.transformation.model.Organization;
import gov.cdc.izgateway.transformation.model.Pipeline;
import gov.cdc.izgateway.transformation.model.Solution;
import gov.cdc.izgateway.transformation.model.User;
import gov.cdc.izgateway.transformation.services.OrganizationService;
import gov.cdc.izgateway.transformation.services.PipelineService;
import gov.cdc.izgateway.transformation.services.SolutionService;
import gov.cdc.izgateway.transformation.services.UserService;
import jakarta.validation.Valid;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.logging.Level;


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
        return pipelineService.getObject(uuid);
    }

    @PutMapping("/api/v1/pipelines/{uuid}")
    public ResponseEntity<Pipeline> updatePipeline(@PathVariable UUID uuid, @RequestBody Pipeline updatedPipeline) {
        updatedPipeline.setId(uuid);
        pipelineService.update(updatedPipeline);
        return new ResponseEntity<>(updatedPipeline, HttpStatus.OK);
    }

    @GetMapping("/api/v1/solutions/{uuid}")
    public ResponseEntity<Solution> getSolutionByUUID(@PathVariable UUID uuid) {
        return solutionService.getObject(uuid);
    }

    @PutMapping("/api/v1/solutions/{uuid}")
    public ResponseEntity<Solution> updateSolution(@PathVariable UUID uuid, @RequestBody Solution updatedSolution) {
        updatedSolution.setId(uuid);
        solutionService.update(updatedSolution);
        return new ResponseEntity<>(updatedSolution, HttpStatus.OK);
    }

    @GetMapping("/api/v1/organizations/{uuid}")
    public ResponseEntity<Organization> getOrganizationByUUID(@PathVariable UUID uuid) {
        return organizationService.getObject(uuid);
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
            return pipelineService.getList(nextCursor, prevCursor, includeInactive, limit);
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
            return organizationService.getList(nextCursor, prevCursor, includeInactive, limit);

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
            return solutionService.getList(nextCursor, prevCursor, includeInactive, limit);
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
            return userService.getList(nextCursor, prevCursor, includeInactive, limit);
        } catch (JsonProcessingException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/api/v1/users/{uuid}")
    public ResponseEntity<User> getUserByUUID(@PathVariable UUID uuid) {
        return userService.getObject(uuid);
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

}
