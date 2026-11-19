package gov.cdc.izgateway.xform.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import gov.cdc.izgateway.security.AccessControlRegistry;
import gov.cdc.izgateway.xform.model.Solution;
import gov.cdc.izgateway.xform.security.Roles;
import gov.cdc.izgateway.xform.services.PipelineService;
import gov.cdc.izgateway.xform.services.SolutionService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.logging.Level;

/**
 * REST API for the solutions.
 */
@Log
@Lazy(false)
@RestController
public class SolutionApiController extends BaseApiController {
    private final SolutionService solutionService;
    private final PipelineService pipelineService;
    /**
     * Creates the controller and registers it with the access control registry.
     *
     * @param solutionService the service for the solutions
     * @param pipelineService the service for the pipelines, used to find solutions that are in use
     * @param registry the access control registry
     */
    @Autowired
    public SolutionApiController(
            SolutionService solutionService,
            PipelineService pipelineService,
            AccessControlRegistry registry
    ) {
        this.solutionService = solutionService;
        this.pipelineService = pipelineService;
        registry.register(this);
    }

    /**
     * Gets one solution.
     *
     * @param uuid the id of the solution
     * @return the solution, or status 404 if it is not found
     */
    @RolesAllowed({Roles.SOLUTION_READER})
    @GetMapping("/api/v1/solutions/{uuid}")
    public ResponseEntity<Solution> getSolutionByUUID(@PathVariable UUID uuid) {
        Solution entity = solutionService.getObject(uuid);
        if (entity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(entity, HttpStatus.OK);
    }

    /**
     * Replaces a solution.
     *
     * @param uuid the id of the solution
     * @param updatedSolution the new solution
     * @return the updated solution
     */
    @RolesAllowed({Roles.SOLUTION_WRITER})
    @PutMapping("/api/v1/solutions/{uuid}")
    public ResponseEntity<Solution> updateSolution(@PathVariable UUID uuid, @Valid @RequestBody Solution updatedSolution) {
        updatedSolution.setId(uuid);
        solutionService.update(updatedSolution);
        return new ResponseEntity<>(updatedSolution, HttpStatus.OK);
    }

    /**
     * Gets one page of solutions.
     *
     * @param nextCursor the id of the solution after which the page starts, or null
     * @param prevCursor the id of the solution before which the page ends, or null
     * @param includeInactive if true, inactive solutions are included
     * @param limit the maximum number of solutions in the page
     * @return the page as JSON, or status 500 if the page cannot be serialized
     */
    @RolesAllowed({Roles.SOLUTION_READER})
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

    /**
     * Creates a solution with a new id.
     *
     * @param solution the solution to create
     * @return the created solution
     */
    @RolesAllowed({Roles.SOLUTION_WRITER})
    @PostMapping("/api/v1/solutions")
    public ResponseEntity<Solution> createSolution(
            @Valid @RequestBody() Solution solution
    ) {
        solutionService.create(solution);
        return new ResponseEntity<>(solution, HttpStatus.OK);
    }

    /**
     * Deletes a solution if no pipeline uses it.
     *
     * @param uuid the id of the solution
     * @return status 204, or status 409 if a pipeline uses the solution
     */
    @RolesAllowed({Roles.SOLUTION_DELETER})
    @DeleteMapping("/api/v1/solutions/{uuid}")
    public ResponseEntity<Solution> deleteSolution(
            @PathVariable UUID uuid
    ) {

        if (pipelineService.isSolutionInUse(uuid)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        solutionService.delete(uuid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
