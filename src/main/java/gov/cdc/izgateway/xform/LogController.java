package gov.cdc.izgateway.xform;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import gov.cdc.izgateway.logging.LogstashMessageSerializer;
import gov.cdc.izgateway.logging.MemoryAppender;
import gov.cdc.izgateway.logging.event.LogEvent;
import gov.cdc.izgateway.security.AccessControlRegistry;
import gov.cdc.izgateway.security.Roles;
import gov.cdc.izgateway.LogControllerBase;
import gov.cdc.izgateway.utils.ListConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
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

	private MemoryAppender logData = null;

	@Autowired
	public LogController(AccessControlRegistry registry) {
        super(registry);
	}


}
