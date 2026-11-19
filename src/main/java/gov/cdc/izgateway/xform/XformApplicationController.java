package gov.cdc.izgateway.xform;

import gov.cdc.izgateway.common.HealthService;
import gov.cdc.izgateway.security.AccessControlRegistry;
import gov.cdc.izgateway.xform.security.Roles;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for the health and build information of the application.
 */
@Log
@RestController
@Lazy(false)
public class XformApplicationController {

    /**
     * Creates the controller and registers it for access control.
     *
     * @param registry the access control registry
     */
    @Autowired
    public XformApplicationController(
            AccessControlRegistry registry
    ) {
        registry.register(this);
    }

    /**
     * Returns the health status of the application.
     *
     * @return true with status 200 if healthy, false with status 503 if not
     */
    @RolesAllowed({Roles.PUBLIC_ACCESS})
    @GetMapping("/healthy")
    public ResponseEntity<Boolean> isHealthy() {
        boolean healthy = HealthService.getHealth().isHealthy();
        if (!healthy) {
            return ResponseEntity.status(503).body(false);
        }
        return ResponseEntity.ok(true);
    }

    /**
     * Returns the health details of the application.
     *
     * @return the health details
     */
    @RolesAllowed({Roles.ADMIN})
    @GetMapping("/health")
    public gov.cdc.izgateway.logging.event.Health getHealth() {
        return HealthService.getHealth();
    }

    /**
     * Returns the build information of the application.
     *
     * @return the build number, build machine and build date as text
     */
    @Operation(summary = "Get build information about the application",
            description = "Returns the build number, build machine and build date.")
    @ApiResponse(responseCode = "200", description = "Success",
            content = @Content(mediaType = "text/plain")
    )
    @RolesAllowed({Roles.ADMIN})
    @GetMapping({"/build", "/build.txt"})
    public String getBuild() {
        return Application.getPage(Application.BUILD);
    }
}
