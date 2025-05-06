package gov.cdc.izgateway.xform;

import gov.cdc.izgateway.common.HealthService;
import gov.cdc.izgateway.security.AccessControlRegistry;
import gov.cdc.izgateway.xform.security.Roles;
import gov.cdc.izgateway.xform.services.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Log
@RestController
@Lazy(false)
public class XformApplicationController {

    @Autowired
    public XformApplicationController(
            AccessControlRegistry registry
    ) {
        registry.register(this);
    }

    @RolesAllowed({Roles.ANY})
    @GetMapping("/healthy")
    public ResponseEntity<Boolean> isHealthy() {
        boolean healthy = HealthService.getHealth().isHealthy();
        if (!healthy) {
            return ResponseEntity.status(503).body(false);
        }
        return ResponseEntity.ok(true);
    }

    @RolesAllowed({Roles.ADMIN})
    @GetMapping("/health")
    public gov.cdc.izgateway.logging.event.Health getHealth() {
        return HealthService.getHealth();
    }

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
