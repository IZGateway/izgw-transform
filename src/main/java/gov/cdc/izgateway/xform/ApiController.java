package gov.cdc.izgateway.xform;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.cdc.izgateway.security.AccessControlRegistry;
import gov.cdc.izgateway.xform.api.BaseApiController;
import gov.cdc.izgateway.xform.model.*;
import gov.cdc.izgateway.xform.model.Mapping;
import gov.cdc.izgateway.xform.security.Roles;
import gov.cdc.izgateway.xform.services.*;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.logging.Level;

@Log
@RestController
@Lazy(false)
public class ApiController extends BaseApiController {
    private final OrganizationService organizationService;
    private final PipelineService pipelineService;
    private final SolutionService solutionService;
    private final MappingService mappingService;
    private final PreconditionService preconditionService;
    private final OperationService operationService;
    private final OperationPreconditionFieldService operationPreconditionFieldService;
    private final GroupRoleMappingService groupRoleMappingService;
    private final AccessControlService accessControlService;

    @Value("${xform.allow-delete-via-api}")
    private Boolean allowDelete;

    @Autowired
    public ApiController(
            OrganizationService organizationService,
            PipelineService pipelineService,
            SolutionService solutionService,
            MappingService mappingService,
            PreconditionService preconditionService,
            OperationService operationService,
            OperationPreconditionFieldService operationPreconditionFieldService,
            GroupRoleMappingService groupRoleMappingService,
            AccessControlService accessControlService,
            AccessControlRegistry registry
    ) {
        this.organizationService = organizationService;
        this.pipelineService = pipelineService;
        this.solutionService = solutionService;
        this.mappingService = mappingService;
        this.preconditionService = preconditionService;
        this.operationService = operationService;
        this.operationPreconditionFieldService = operationPreconditionFieldService;
        this.groupRoleMappingService = groupRoleMappingService;
        this.accessControlService = accessControlService;
        registry.register(this);
    }

    @RolesAllowed({Roles.PIPELINE_READER})
    @GetMapping("/api/v1/pipelines/{uuid}")
    public ResponseEntity<Pipeline> getPipelineByUUID(@PathVariable UUID uuid) {
        Pipeline entity = pipelineService.getObject(uuid);
        if (entity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(entity, HttpStatus.OK);
    }

    @RolesAllowed({Roles.PIPELINE_WRITER})
    @PutMapping("/api/v1/pipelines/{uuid}")
    public ResponseEntity<Pipeline> updatePipeline(@PathVariable UUID uuid, @Valid @RequestBody Pipeline updatedPipeline) {
        updatedPipeline.setId(uuid);
        pipelineService.update(updatedPipeline);
        return new ResponseEntity<>(updatedPipeline, HttpStatus.OK);
    }

    @RolesAllowed({Roles.SOLUTION_READER})
    @GetMapping("/api/v1/solutions/{uuid}")
    public ResponseEntity<Solution> getSolutionByUUID(@PathVariable UUID uuid) {
        Solution entity = solutionService.getObject(uuid);
        if (entity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(entity, HttpStatus.OK);
    }

    @RolesAllowed({Roles.SOLUTION_WRITER})
    @PutMapping("/api/v1/solutions/{uuid}")
    public ResponseEntity<Solution> updateSolution(@PathVariable UUID uuid, @Valid @RequestBody Solution updatedSolution) {
        updatedSolution.setId(uuid);
        solutionService.update(updatedSolution);
        return new ResponseEntity<>(updatedSolution, HttpStatus.OK);
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

    @RolesAllowed({Roles.ORGANIZATION_WRITER})
    @PutMapping("/api/v1/organizations/{uuid}")
    public ResponseEntity<Organization> updateOrganization(@PathVariable UUID uuid, @RequestBody Organization updatedOrganization) {
        updatedOrganization.setId(uuid);
        organizationService.update(updatedOrganization);
        return new ResponseEntity<>(updatedOrganization, HttpStatus.OK);
    }

    // TODO - Paul to look at AccessControl as it works with groups too.
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

    @RolesAllowed({Roles.SOLUTION_READER})
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
        if (Boolean.FALSE.equals(allowDelete)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        groupRoleMappingService.delete(uuid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    @RolesAllowed({Roles.ADMIN})
    @GetMapping("/api/v1/access-controls/{uuid}")
    public ResponseEntity<AccessControl> getAccessControlByUUID(@PathVariable UUID uuid) {
        AccessControl entity = accessControlService.getObject(uuid);
        if (entity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(entity, HttpStatus.OK);
    }

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

    @RolesAllowed({Roles.ADMIN})
    @PostMapping("/api/v1/access-controls")
    public ResponseEntity<AccessControl> createAccessControl(
            @Valid @RequestBody() AccessControl accessControl
    ) {
        accessControlService.create(accessControl);
        return new ResponseEntity<>(accessControl, HttpStatus.OK);
    }

    @RolesAllowed({Roles.ADMIN})
    @DeleteMapping("/api/v1/access-controls/{uuid}")
    public ResponseEntity<AccessControl> deleteAccessControl(
            @PathVariable UUID uuid
    ) {
        if (Boolean.FALSE.equals(allowDelete)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        accessControlService.delete(uuid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

        @RolesAllowed({Roles.SOLUTION_READER})
    @GetMapping("/api/v1/mappings/{uuid}")
    public ResponseEntity<Mapping> getMappingByUUID(@PathVariable UUID uuid) {
        Mapping entity = mappingService.getObject(uuid);
        if (entity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(entity, HttpStatus.OK);
    }

    @RolesAllowed({Roles.SOLUTION_WRITER})
    @PostMapping("/api/v1/mappings")
    public ResponseEntity<Mapping> createMapping(
            @Valid @RequestBody() Mapping mapping
    ) {
        mappingService.create(mapping);
        return new ResponseEntity<>(mapping, HttpStatus.OK);
    }

    @RolesAllowed({Roles.SOLUTION_WRITER})
    @PutMapping("/api/v1/mappings/{uuid}")
    public ResponseEntity<Mapping> updateMapping(
            @PathVariable UUID uuid,
            @RequestBody @Valid Mapping updatedMapping
    ) {
        updatedMapping.setId(uuid);
        mappingService.update(updatedMapping);
        return new ResponseEntity<>(updatedMapping, HttpStatus.OK);
    }

    @RolesAllowed({Roles.SOLUTION_DELETER})
    @DeleteMapping("/api/v1/mappings/{uuid}")
    public ResponseEntity<Mapping> deleteMapping(
            @PathVariable UUID uuid
    ) {
        if (Boolean.FALSE.equals(allowDelete)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        mappingService.delete(uuid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RolesAllowed({Roles.PIPELINE_WRITER})
    @PostMapping("/api/v1/pipelines")
    public ResponseEntity<Pipeline> createPipeline(
            @Valid @RequestBody() Pipeline pipeline
    ) {
        pipelineService.create(pipeline);
        return new ResponseEntity<>(pipeline, HttpStatus.OK);
    }

    @RolesAllowed({Roles.ORGANIZATION_WRITER})
    @PostMapping("/api/v1/organizations")
    public ResponseEntity<Organization> createOrganization(
            @Valid @RequestBody() Organization organization
    ) {
        organizationService.create(organization);
        return new ResponseEntity<>(organization, HttpStatus.OK);
    }

    @RolesAllowed({Roles.SOLUTION_WRITER})
    @PostMapping("/api/v1/solutions")
    public ResponseEntity<Solution> createSolution(
            @Valid @RequestBody() Solution solution
    ) {
        solutionService.create(solution);
        return new ResponseEntity<>(solution, HttpStatus.OK);
    }

    @RolesAllowed({Roles.PIPELINE_DELETER})
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

    @RolesAllowed({Roles.SOLUTION_DELETER})
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

    @RolesAllowed({Roles.ORGANIZATION_DELETER})
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

    @RolesAllowed({Roles.SOLUTION_READER})
    @GetMapping("/api/v1/operations/available")
    public ResponseEntity<List<OperationInfo>> getAvailableOperationList() {
        try {
            return ResponseEntity.ok(operationService.getList());
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RolesAllowed({Roles.PIPELINE_READER})
    @GetMapping("/api/v1/preconditions/available")
    public ResponseEntity<List<PreconditionInfo>> getAvailablePreconditionList() {
        try {
            return ResponseEntity.ok(preconditionService.getList());
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RolesAllowed({Roles.SOLUTION_READER})
    @GetMapping(
            {
                    "/api/v1/{fieldType}/fields"
                    ,"/api/v1/fields"
            }
    )
    public ResponseEntity<String> getFieldsList(
            @PathVariable(required = false) String fieldType,
            @RequestParam(required = false) String nextCursor,
            @RequestParam(required = false) String prevCursor,
            @RequestParam(defaultValue = "false") Boolean includeInactive,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {

            if (fieldType != null && fieldType.equals("operations")) {
                return processList(operationPreconditionFieldService.getOperationList(), nextCursor, prevCursor, includeInactive, limit);
            } else if (fieldType != null && fieldType.equals("preconditions")) {
                return processList(operationPreconditionFieldService.getPreconditionList(), nextCursor, prevCursor, includeInactive, limit);
            } else {
                return processList(operationPreconditionFieldService.getList(), nextCursor, prevCursor, includeInactive, limit);
            }

        } catch (JsonProcessingException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RolesAllowed({Roles.SOLUTION_READER})
    @GetMapping("/api/v1/fields/{uuid}")
    public ResponseEntity<Object> getFieldByUUID(
            @PathVariable UUID uuid
    ) {
        OperationPreconditionField entity = operationPreconditionFieldService.getObject(uuid);

        if (entity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(entity, HttpStatus.OK);
    }

    @RolesAllowed({Roles.SOLUTION_WRITER})
    @PutMapping("/api/v1/fields/{uuid}")
    public ResponseEntity<Object> updateField(
            @PathVariable UUID uuid,
            @RequestBody @Valid OperationPreconditionField updatedPreconditionField
    ) {
        updatedPreconditionField.setId(uuid);
        operationPreconditionFieldService.update(updatedPreconditionField);
        return new ResponseEntity<>(updatedPreconditionField, HttpStatus.OK);
    }

    @RolesAllowed({Roles.SOLUTION_WRITER})
    @PostMapping("/api/v1/fields")
    public ResponseEntity<OperationPreconditionField> createField(
            @Valid @RequestBody() OperationPreconditionField operationPreconditionField
    ) {
        operationPreconditionFieldService.create(operationPreconditionField);
        return new ResponseEntity<>(operationPreconditionField, HttpStatus.OK);
    }

    @RolesAllowed({Roles.SOLUTION_DELETER})
    @DeleteMapping("/api/v1/fields/{uuid}")
    public ResponseEntity<OperationPreconditionField> deleteFieldByUUID(
            @PathVariable UUID uuid
    ) {
        if (Boolean.FALSE.equals(allowDelete)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        operationPreconditionFieldService.delete(uuid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
