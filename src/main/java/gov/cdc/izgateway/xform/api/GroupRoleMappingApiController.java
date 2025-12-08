package gov.cdc.izgateway.xform.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import gov.cdc.izgateway.security.AccessControlRegistry;
import gov.cdc.izgateway.xform.model.GroupRoleMapping;
import gov.cdc.izgateway.xform.security.Roles;
import gov.cdc.izgateway.xform.services.GroupRoleMappingService;
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
public class GroupRoleMappingApiController extends BaseApiController {
    private final GroupRoleMappingService groupRoleMappingService;

    @Autowired
    public GroupRoleMappingApiController(
            GroupRoleMappingService groupRoleMappingService,
            AccessControlRegistry registry
    ) {
        this.groupRoleMappingService = groupRoleMappingService;
        registry.register(this);
    }

    @RolesAllowed({Roles.ADMIN})
    @GetMapping("/api/v1/group-role-mappings")
    public ResponseEntity<String> getGroupRoleMappingList(
            @RequestParam(required = false) String nextCursor,
            @RequestParam(required = false) String prevCursor,
            @RequestParam(defaultValue = "false") Boolean includeInactive,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            return processList(groupRoleMappingService.getList(), nextCursor, prevCursor, includeInactive, limit);
        } catch (JsonProcessingException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RolesAllowed({Roles.ADMIN})
    @GetMapping("/api/v1/group-role-mappings/{uuid}")
    public ResponseEntity<GroupRoleMapping> getGroupRoleMappingByUUID(@PathVariable UUID uuid) {
        GroupRoleMapping entity = groupRoleMappingService.getObject(uuid);
        if (entity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(entity, HttpStatus.OK);
    }

    @RolesAllowed({Roles.ADMIN})
    @PutMapping("/api/v1/group-role-mappings/{uuid}")
    public ResponseEntity<GroupRoleMapping> updateGroupRoleMapping(
            @PathVariable UUID uuid,
            @RequestBody @Valid GroupRoleMapping updatedGroupRoleMapping
    ) {
        updatedGroupRoleMapping.setId(uuid);
        groupRoleMappingService.update(updatedGroupRoleMapping);
        return new ResponseEntity<>(updatedGroupRoleMapping, HttpStatus.OK);
    }

    @RolesAllowed({Roles.ADMIN})
    @PostMapping("/api/v1/group-role-mappings")
    public ResponseEntity<GroupRoleMapping> createGroupRoleMapping(
            @Valid @RequestBody() GroupRoleMapping groupRoleMapping
    ) {
        groupRoleMappingService.create(groupRoleMapping);
        return new ResponseEntity<>(groupRoleMapping, HttpStatus.OK);
    }

    @RolesAllowed({Roles.ADMIN})
    @DeleteMapping("/api/v1/group-role-mappings/{uuid}")
    public ResponseEntity<GroupRoleMapping> deleteGroupRoleMapping(
            @PathVariable UUID uuid
    ) {
        groupRoleMappingService.delete(uuid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
