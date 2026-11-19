package gov.cdc.izgateway.xform.api;

import gov.cdc.izgateway.security.AccessControlRegistry;
import gov.cdc.izgateway.xform.model.PreconditionInfo;
import gov.cdc.izgateway.xform.security.Roles;
import gov.cdc.izgateway.xform.services.PreconditionService;
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
 * REST API that lists the available precondition types.
 */
@Log
@Lazy(false)
@RestController
public class PreconditionApiController extends BaseApiController {
    private final PreconditionService preconditionService;

    /**
     * Creates the controller and registers it with the access control registry.
     *
     * @param preconditionService the service that lists the precondition types
     * @param registry the access control registry
     */
    @Autowired
    public PreconditionApiController(
            PreconditionService preconditionService,
            AccessControlRegistry registry
    ) {
        this.preconditionService = preconditionService;
        registry.register(this);
    }

    /**
     * Gets the precondition types and the properties that each type uses.
     *
     * @return the precondition types, or status 500 if an error occurs
     */
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
}
