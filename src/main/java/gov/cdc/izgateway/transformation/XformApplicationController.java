package gov.cdc.izgateway.transformation;

import gov.cdc.izgateway.common.HealthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.java.Log;
import org.springframework.web.bind.annotation.*;

@Log
@RestController
public class XformApplicationController {

    @GetMapping("/health")
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
