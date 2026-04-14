package gov.cdc.izgateway.xform.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import gov.cdc.izgateway.security.AccessControlRegistry;
import gov.cdc.izgateway.xform.model.AccessControl;
import gov.cdc.izgateway.xform.security.Roles;
import gov.cdc.izgateway.xform.services.AccessControlService;
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
 * REST API for managing access control entries.
 */
@Log
@Lazy(false)
@RestController
public class AccessControlApiController extends BaseApiController {
    private final AccessControlService accessControlService;

    /**
     * Create the access control API controller.
     *
     * @param accessControlService Service for access control operations
     * @param registry Registry to register this controller for access control
     */
    @Autowired
    public AccessControlApiController(
            AccessControlService accessControlService,
            AccessControlRegistry registry
    ) {
        this.accessControlService = accessControlService;
        registry.register(this);
    }

    /**
     * List access control entries with optional pagination.
     *
     * @param nextCursor Cursor for the next page
     * @param prevCursor Cursor for the previous page
     * @param includeInactive Whether to include inactive entries
     * @param limit Maximum number of entries to return
     * @return A serialized page of access control entries
     */
    @RolesAllowed({Roles.ADMIN})
    @GetMapping("/api/v1/access-controls")
    public ResponseEntity<String> getAccessControlList(
            @RequestParam(required = false) String nextCursor,
            @RequestParam(required = false) String prevCursor,
            @RequestParam(defaultValue = "false") Boolean includeInactive,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            return processList(accessControlService.getList(), nextCursor, prevCursor, includeInactive, limit);
        } catch (JsonProcessingException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Fetch a specific access control entry by id.
     *
     * @param uuid Access control id
     * @return The access control entry if found
     */
    @RolesAllowed({Roles.ADMIN})
    @GetMapping("/api/v1/access-controls/{uuid}")
    public ResponseEntity<AccessControl> getAccessControlByUUID(@PathVariable UUID uuid) {
        AccessControl entity = accessControlService.getObject(uuid);
        if (entity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(entity, HttpStatus.OK);
    }

    /**
     * Update an access control entry.
     *
     * @param uuid Access control id
     * @param updatedAccessControl Updated access control data
     * @return The updated access control entry
     */
    @RolesAllowed({Roles.ADMIN})
    @PutMapping("/api/v1/access-controls/{uuid}")
    public ResponseEntity<AccessControl> updateAccessControl(
            @PathVariable UUID uuid,
            @RequestBody @Valid AccessControl updatedAccessControl
    ) {
        updatedAccessControl.setId(uuid);
        accessControlService.update(updatedAccessControl);
        return new ResponseEntity<>(updatedAccessControl, HttpStatus.OK);
    }

    /**
     * Create a new access control entry.
     *
     * @param accessControl New access control entry
     * @return The created access control entry
     */
    @RolesAllowed({Roles.ADMIN})
    @PostMapping("/api/v1/access-controls")
    public ResponseEntity<AccessControl> createAccessControl(
            @Valid @RequestBody() AccessControl accessControl
    ) {
        accessControlService.create(accessControl);
        return new ResponseEntity<>(accessControl, HttpStatus.OK);
    }

    /**
     * Delete an access control entry.
     *
     * @param uuid Access control id
     * @return HTTP 204 if deleted
     */
    @RolesAllowed({Roles.ADMIN})
    @DeleteMapping("/api/v1/access-controls/{uuid}")
    public ResponseEntity<AccessControl> deleteAccessControl(
            @PathVariable UUID uuid
    ) {
        accessControlService.delete(uuid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
