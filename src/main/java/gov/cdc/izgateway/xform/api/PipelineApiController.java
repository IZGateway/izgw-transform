package gov.cdc.izgateway.xform.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import gov.cdc.izgateway.security.AccessControlRegistry;
import gov.cdc.izgateway.xform.model.Pipeline;
import gov.cdc.izgateway.xform.security.Roles;
import gov.cdc.izgateway.xform.services.PipelineService;
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
 * REST API for the pipelines.
 */
@Log
@Lazy(false)
@RestController
public class PipelineApiController extends BaseApiController {
    private final PipelineService pipelineService;

    /**
     * Creates the controller and registers it with the access control registry.
     *
     * @param pipelineService the service for the pipelines
     * @param registry the access control registry
     */
    @Autowired
    public PipelineApiController(
            PipelineService pipelineService,
            AccessControlRegistry registry
    ) {
        this.pipelineService = pipelineService;
        registry.register(this);
    }

    /**
     * Gets one pipeline.
     *
     * @param uuid the id of the pipeline
     * @return the pipeline, or status 404 if it is not found
     */
    @RolesAllowed({Roles.PIPELINE_READER})
    @GetMapping("/api/v1/pipelines/{uuid}")
    public ResponseEntity<Pipeline> getPipelineByUUID(@PathVariable UUID uuid) {
        Pipeline entity = pipelineService.getObject(uuid);
        if (entity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(entity, HttpStatus.OK);
    }

    /**
     * Replaces a pipeline.
     *
     * @param uuid the id of the pipeline
     * @param updatedPipeline the new pipeline
     * @return the updated pipeline
     */
    @RolesAllowed({Roles.PIPELINE_WRITER})
    @PutMapping("/api/v1/pipelines/{uuid}")
    public ResponseEntity<Pipeline> updatePipeline(@PathVariable UUID uuid, @Valid @RequestBody Pipeline updatedPipeline) {
        updatedPipeline.setId(uuid);
        pipelineService.update(updatedPipeline);
        return new ResponseEntity<>(updatedPipeline, HttpStatus.OK);
    }

    // TODO - Paul to look at AccessControl as it works with groups too.
    /**
     * Gets one page of pipelines.
     *
     * @param nextCursor the id of the pipeline after which the page starts, or null
     * @param prevCursor the id of the pipeline before which the page ends, or null
     * @param includeInactive if true, inactive pipelines are included
     * @param limit the maximum number of pipelines in the page
     * @return the page as JSON, or status 500 if the page cannot be serialized
     */
    @RolesAllowed({Roles.PIPELINE_READER})
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

    /**
     * Creates a pipeline with a new id.
     *
     * @param pipeline the pipeline to create
     * @return the created pipeline
     */
    @RolesAllowed({Roles.PIPELINE_WRITER})
    @PostMapping("/api/v1/pipelines")
    public ResponseEntity<Pipeline> createPipeline(
            @Valid @RequestBody() Pipeline pipeline
    ) {
        pipelineService.create(pipeline);
        return new ResponseEntity<>(pipeline, HttpStatus.OK);
    }

    /**
     * Deletes a pipeline.
     *
     * @param uuid the id of the pipeline
     * @return status 204
     */
    @RolesAllowed({Roles.PIPELINE_DELETER})
    @DeleteMapping("/api/v1/pipelines/{uuid}")
    public ResponseEntity<Pipeline> deletePipeline(
            @PathVariable UUID uuid
    ) {

        pipelineService.delete(uuid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
