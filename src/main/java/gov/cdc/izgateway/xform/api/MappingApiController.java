package gov.cdc.izgateway.xform.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import gov.cdc.izgateway.security.AccessControlRegistry;
import gov.cdc.izgateway.xform.model.Mapping;
import gov.cdc.izgateway.xform.security.Roles;
import gov.cdc.izgateway.xform.services.MappingService;
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
 * REST API for the code mappings.
 */
@Log
@Lazy(false)
@RestController
public class MappingApiController extends BaseApiController {
    private final MappingService mappingService;

    /**
     * Creates the controller and registers it with the access control registry.
     *
     * @param mappingService the service for the code mappings
     * @param registry the access control registry
     */
    @Autowired
    public MappingApiController(
            MappingService mappingService,
            AccessControlRegistry registry
    ) {
        this.mappingService = mappingService;
        registry.register(this);
    }

    /**
     * Gets one page of code mappings.
     *
     * @param nextCursor the id of the mapping after which the page starts, or null
     * @param prevCursor the id of the mapping before which the page ends, or null
     * @param includeInactive if true, inactive mappings are included
     * @param limit the maximum number of mappings in the page
     * @return the page as JSON, or status 500 if the page cannot be serialized
     */
    @RolesAllowed({Roles.PIPELINE_READER})
    @GetMapping("/api/v1/mappings")
    public ResponseEntity<String> getMappingsList(
            @RequestParam(required = false) String nextCursor,
            @RequestParam(required = false) String prevCursor,
            @RequestParam(defaultValue = "false") Boolean includeInactive,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            return processList(mappingService.getList(), nextCursor, prevCursor, includeInactive, limit);
        } catch (JsonProcessingException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gets one code mapping.
     *
     * @param uuid the id of the mapping
     * @return the mapping, or status 404 if it is not found
     */
    @RolesAllowed({Roles.PIPELINE_READER})
    @GetMapping("/api/v1/mappings/{uuid}")
    public ResponseEntity<Mapping> getMappingByUUID(@PathVariable UUID uuid) {
        Mapping entity = mappingService.getObject(uuid);
        if (entity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(entity, HttpStatus.OK);
    }

    /**
     * Creates a code mapping with a new id.
     *
     * @param mapping the mapping to create
     * @return the created mapping
     */
    @RolesAllowed({Roles.PIPELINE_WRITER})
    @PostMapping("/api/v1/mappings")
    public ResponseEntity<Mapping> createMapping(
            @Valid @RequestBody() Mapping mapping
    ) {
        mappingService.create(mapping);
        return new ResponseEntity<>(mapping, HttpStatus.OK);
    }

    /**
     * Replaces a code mapping.
     *
     * @param uuid the id of the mapping
     * @param updatedMapping the new mapping
     * @return the updated mapping
     */
    @RolesAllowed({Roles.PIPELINE_WRITER})
    @PutMapping("/api/v1/mappings/{uuid}")
    public ResponseEntity<Mapping> updateMapping(
            @PathVariable UUID uuid,
            @RequestBody @Valid Mapping updatedMapping
    ) {
        updatedMapping.setId(uuid);
        mappingService.update(updatedMapping);
        return new ResponseEntity<>(updatedMapping, HttpStatus.OK);
    }

    /**
     * Deletes a code mapping.
     *
     * @param uuid the id of the mapping
     * @return status 204
     */
    @RolesAllowed({Roles.PIPELINE_DELETER})
    @DeleteMapping("/api/v1/mappings/{uuid}")
    public ResponseEntity<Mapping> deleteMapping(
            @PathVariable UUID uuid
    ) {
        mappingService.delete(uuid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
