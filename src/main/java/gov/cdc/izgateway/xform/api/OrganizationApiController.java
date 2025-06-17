package gov.cdc.izgateway.xform.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import gov.cdc.izgateway.security.AccessControlRegistry;
import gov.cdc.izgateway.xform.model.Organization;
import gov.cdc.izgateway.xform.security.Roles;
import gov.cdc.izgateway.xform.services.OrganizationService;
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

@Log
@Lazy(false)
@RestController
public class OrganizationApiController extends BaseApiController {
    private final OrganizationService organizationService;

    @Value("${xform.allow-delete-via-api}")
    private Boolean allowDelete;

    @Autowired
    public OrganizationApiController(
            OrganizationService organizationService,
            AccessControlRegistry registry
    ) {
        this.organizationService = organizationService;
        registry.register(this);
    }

    @RolesAllowed({Roles.ORGANIZATION_READER})
    @GetMapping("/api/v1/organizations/{uuid}")
    public ResponseEntity<Organization> getOrganizationByUUID(@PathVariable UUID uuid) {
        Organization entity = organizationService.getObject(uuid);
        if (entity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(entity, HttpStatus.OK);
    }

    @RolesAllowed({Roles.ADMIN})
    @PutMapping("/api/v1/organizations/{uuid}")
    public ResponseEntity<Organization> updateOrganization(@PathVariable UUID uuid, @RequestBody Organization updatedOrganization) {
        updatedOrganization.setId(uuid);
        organizationService.update(updatedOrganization);
        return new ResponseEntity<>(updatedOrganization, HttpStatus.OK);
    }

    @RolesAllowed({Roles.ORGANIZATION_READER})
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

    @RolesAllowed({Roles.ADMIN})
    @PostMapping("/api/v1/organizations")
    public ResponseEntity<Organization> createOrganization(
            @Valid @RequestBody() Organization organization
    ) {
        organizationService.create(organization);
        return new ResponseEntity<>(organization, HttpStatus.OK);
    }

    @RolesAllowed({Roles.ADMIN})
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
