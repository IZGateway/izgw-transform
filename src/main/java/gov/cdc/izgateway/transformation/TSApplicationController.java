package gov.cdc.izgateway.transformation;

import gov.cdc.izgateway.common.HealthService;
import gov.cdc.izgateway.transformation.annotations.CaptureXformAdvice;
import gov.cdc.izgateway.security.AccessControlRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

@Log
@RestController
@Lazy(false)
@RolesAllowed({"admin"})
public class TSApplicationController {

    @Autowired
    public TSApplicationController(AccessControlRegistry registry) {
        registry.register(this);
    }

    @GetMapping("/health")
    //@RolesAllowed({"admin", "superuser", "ROLE_FROM_CERT"})
    @RolesAllowed({"admin"})
    public gov.cdc.izgateway.logging.event.Health getHealth() {
        return HealthService.getHealth();
    }

    @Operation(summary = "Get build information about the application",
            description = "Returns the build number, build machine and build date.")
    @ApiResponse(responseCode = "200", description = "Success",
            content = @Content(mediaType = "text/plain")
    )
    @GetMapping({"/build", "/build.txt"})
    public String getBuild() {
        return Application.getPage(Application.BUILD);
    }
}
