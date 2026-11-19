package gov.cdc.izgateway.xform.api;

import gov.cdc.izgateway.security.AccessControlRegistry;
import gov.cdc.izgateway.xform.model.OperationInfo;
import gov.cdc.izgateway.xform.security.Roles;
import gov.cdc.izgateway.xform.services.OperationService;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.logging.Level;

/**
 * REST API that lists the available operation types.
 */
@Log
@Lazy(false)
@RestController
public class OperationApiController extends BaseApiController {
    private final OperationService operationService;

    /**
     * Creates the controller and registers it with the access control registry.
     *
     * @param operationService the service that lists the operation types
     * @param registry the access control registry
     */
    @Autowired
    public OperationApiController(
            OperationService operationService,
            AccessControlRegistry registry
    ) {
        this.operationService = operationService;
        registry.register(this);
    }

    /**
     * Gets the operation types and the properties that each type uses.
     *
     * @return the operation types, or status 500 if an error occurs
     */
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
}
