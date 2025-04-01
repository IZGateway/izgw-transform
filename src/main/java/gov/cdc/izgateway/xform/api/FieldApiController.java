package gov.cdc.izgateway.xform.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import gov.cdc.izgateway.security.AccessControlRegistry;
import gov.cdc.izgateway.xform.model.OperationPreconditionField;
import gov.cdc.izgateway.xform.security.Roles;
import gov.cdc.izgateway.xform.services.OperationPreconditionFieldService;
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
public class FieldApiController extends BaseApiController {
    private final OperationPreconditionFieldService operationPreconditionFieldService;

    @Value("${xform.allow-delete-via-api}")
    private Boolean allowDelete;

    @Autowired
    public FieldApiController(
            OperationPreconditionFieldService operationPreconditionFieldService,
            AccessControlRegistry registry
    ) {
        this.operationPreconditionFieldService = operationPreconditionFieldService;
        registry.register(this);
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
