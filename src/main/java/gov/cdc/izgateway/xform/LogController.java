package gov.cdc.izgateway.xform;

import gov.cdc.izgateway.logging.event.LogEvent;
import gov.cdc.izgateway.security.AccessControlRegistry;
import gov.cdc.izgateway.security.Roles;
import gov.cdc.izgateway.LogControllerBase;
import gov.cdc.izgateway.soap.fault.SecurityFault;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * The LogController provides access to in memory logging data on a server.
 * This is used for integration testing to verify log content is as expected
 * when sending messages.
 */

@RestController
@CrossOrigin
// TODO: Presently, blacklisted users are allowed to access the logs request, b/c blacklisting only
// applies to the SOAP Stack.  Once we apply it to the full HTTP stack, we will have to provide
// SECURE mechanism for clearing the blacklisted state of the testing user.  It cannot be said
// to have been applied to the full stack until this loophole is resolved.
@RolesAllowed({ Roles.ADMIN, Roles.OPERATIONS, Roles.BLACKLIST })
@RequestMapping({"/rest"})
@Lazy(false)
public class LogController extends LogControllerBase {

	@Autowired
	public LogController(AccessControlRegistry registry) {
        super(registry);
	}

    @Operation(summary = "Get the most recent log records",
            description = "Search for the log records matching the search parameter or all records if there is no search value")
    @ApiResponse(responseCode = "200", description = "Success",
            content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = LogEvent.class)))
            }
    )
    @GetMapping("/logs")
    @Override
    public List<LogEvent> getLogs(
            @Parameter(description = "The search string")
            @RequestParam(required = false) String search) {
        return super.getLogs(search);
    }

    @Operation(summary = "Clear log records")
    @ApiResponse(responseCode = "204", description = "Reset the logs", content = @Content)
    @DeleteMapping("/logs")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RolesAllowed({ Roles.ADMIN, Roles.OPERATIONS, Roles.BLACKLIST })
    @Override
    public void deleteLogs(HttpServletRequest servletReq,
                           @Parameter(description="If true, reset the specified endpoint, clearing maintenance")
                           @RequestParam(required = false) String clear) throws SecurityFault {
        super.deleteLogs(servletReq, clear);
    }
}
