package gov.cdc.izgateway.transformation;
import com.fasterxml.jackson.core.JsonProcessingException;
import gov.cdc.izgateway.transformation.model.Organization;
import gov.cdc.izgateway.transformation.services.OrganizationService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.logging.Level;


@Log
@RestController
public class ApiController {
    private final OrganizationService organizationService;

    @Autowired
    public ApiController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping("/api/v1/organizations/{uuid}")
    public ResponseEntity<Organization> getOrganizationByUUID(@PathVariable UUID uuid) {
        return organizationService.getOrganizationResponse(uuid);
    }

    @PutMapping("/api/v1/organizations/{uuid}")
    public ResponseEntity<Organization> updateOrganization(@PathVariable UUID uuid, @RequestBody Map<String, Object> updatedOrganization) {
        Organization newOrg = new Organization(UUID.fromString(updatedOrganization.get("organizationId").toString()), updatedOrganization.get("organizationName").toString(), (Boolean) updatedOrganization.get("active"));
        organizationService.updateOrganization(newOrg);
        return new ResponseEntity<>(newOrg, HttpStatus.OK);
    }

    @GetMapping("/api/v1/organizations")
    public ResponseEntity<String> getOrganizationsList(
        @RequestParam(required = false) String nextCursor,
        @RequestParam(required = false) String prevCursor,
        @RequestParam(defaultValue = "false") Boolean includeInactive,
        @RequestParam(defaultValue = "10") int limit) {
        try {
            return organizationService.getOrganizationList(nextCursor, prevCursor, includeInactive, limit);

        } catch (JsonProcessingException e) { //need to keep this part of the logic because log.log is dependent on @Log
            log.log(Level.SEVERE, e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/v1/organizations")
    public ResponseEntity<Organization> createOrganization(
            @RequestBody() String orgName
             ){
            return new ResponseEntity<>(organizationService.createOrganization(orgName), HttpStatus.OK);
    }
}
