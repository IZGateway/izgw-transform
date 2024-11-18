package gov.cdc.izgateway.soap;

import gov.cdc.izgateway.common.HasDestinationUri;
import gov.cdc.izgateway.configuration.AppProperties;
import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.logging.event.TransactionData;
import gov.cdc.izgateway.logging.event.TransactionData.MessageType;
import gov.cdc.izgateway.logging.info.MessageInfo;
import gov.cdc.izgateway.logging.info.MessageInfo.Direction;
import gov.cdc.izgateway.logging.info.MessageInfo.EndpointType;
import gov.cdc.izgateway.logging.markers.Markers2;
import gov.cdc.izgateway.model.IDestination;
import gov.cdc.izgateway.model.IMessageHeader;
import gov.cdc.izgateway.model.RetryStrategy;
import gov.cdc.izgateway.security.Roles;
import gov.cdc.izgateway.service.IDestinationService;
import gov.cdc.izgateway.service.IMessageHeaderService;
import gov.cdc.izgateway.soap.fault.*;
import gov.cdc.izgateway.soap.message.*;
import gov.cdc.izgateway.soap.message.SoapMessage.Request;
import gov.cdc.izgateway.soap.net.SoapMessageConverter.SoapConversionException;
import gov.cdc.izgateway.soap.net.SoapMessageReader.SoapParseException;
import gov.cdc.izgateway.utils.HL7Utils.HL7Message;
import gov.cdc.izgateway.utils.JsonUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/*
    ToDo: Paul - get this version of SoapControllerBase.java to compile and run without the need for destinations,
          or anything.  Just make this be as dumb as possible to use as a passthrough.  If it works, you'll
          need to refactor core with this version, etc.
 */

/**
 * This is the base class for the IZ Gateway Hub and the Mock Controllers.  These controllers perform the same functions save that the Hub routes requests, and the Mock executes on them as if
 * it were an actual IIS.
 */
@Slf4j
public abstract class SoapControllerBase {

	private static Map<String, String> resourceCache = new HashMap<>();
	private static final List<String> XSD_FILES = Arrays.asList("cdc-iis.xsd", "cdc-iis-2011.xsd", "cdc-iis-hub.xsd");

	protected final IMessageHeaderService mshService;
	@Getter
	private final String messageNamespace;
	@Getter
	private final List<String> faultNamespaces;
	@Getter
	private final String wsdl;
	@Getter
	private final boolean usingHubHeaders;
	@Getter
	private final String serviceType;
	@Getter
	@Value("${server.hostname}")
	private String serverName;
	@Getter
	@Value("${server.mode:prod}")
	private String mode;

	/**
	 * Catch and kill transactions to non-prod environments when test data does
	 * not follow known test patterns.
	 */
	@Getter
	@Value("${server.cnk-enabled:false}")
	private boolean catchAndKillEnabled;

  @Getter
	@Setter
	private int maxMessageSize = 65536;

	protected IDestinationService getDestinationService() {
		return null;
	}

	protected final boolean isHub() {
		return getDestinationService() != null;
	}
	protected abstract void checkCredentials(HasCredentials s) throws SecurityFault;
	
	/**
	 * A functional interface for a task that can throw a fault.
	 * 
	 * @author Audacious Inquiry
	 *
	 * @param <V>	The return type of the task.
	 */
	public interface Work<V> {
		/**
		 * Computes a result, or throws an exception if unable to do so.
		 *
		 * @return computed result
		 * @throws Fault if unable to compute a result
		 */
		V call() throws Fault;
	}
	protected SoapControllerBase(IMessageHeaderService mshService, String messageNamespace, String wsdl, List<String> faultNamespaces) {
		this.mshService = mshService;
		this.messageNamespace = messageNamespace;
		this.wsdl = wsdl;
		this.faultNamespaces = faultNamespaces == null ? Collections.singletonList(messageNamespace) : Collections.unmodifiableList(faultNamespaces);
		this.usingHubHeaders = faultNamespaces != null;
		this.serviceType = usingHubHeaders ? "Gateway" : "Mock";
	}

	/**
	 * Log the start content for sending a SOAP Message
	 * @param soapMessage	The message being sent
	 */
	public void logStartOfRequest(SoapMessage soapMessage) {
		TransactionData tData = RequestContext.getTransactionData();
		String message;
		if (soapMessage != null) {
			soapMessage.updateAction(isHub());
			if (soapMessage instanceof SubmitSingleMessageRequest s) {
				tData.setMessageType(MessageType.SUBMIT_SINGLE_MESSAGE);
				message = s.getHl7Message();
				tData.getSource().setType(getSourceType(message));
				tData.setRequestHL7Message(message);
			} else if (soapMessage instanceof ConnectivityTestRequest c) {
				tData.setMessageType(MessageType.CONNECTIVITY_TEST);
				message = c.getEchoBack();
				tData.setRequestEchoBack(message);
			} else {
				tData.setMessageType(MessageType.OTHER);
			}

			if (soapMessage instanceof Request) {
				MessageInfo messageInfo = new MessageInfo(soapMessage, EndpointType.SERVER, Direction.INBOUND, AppProperties.isProduction());
				tData.setMessageId(soapMessage.getWsaHeaders().getMessageID());
				tData.setReplyTo(soapMessage.getWsaHeaders().getRelatesTo());
				tData.setResponseReceived(false);
				tData.getServerRequest().setWs_request_message(messageInfo);
				messageInfo.setHttpHeaders(RequestContext.getHttpHeaders());
			}
			if (soapMessage instanceof HasFacilityID hfid) {
				tData.getSource().setFacilityId(hfid.getFacilityID());
			}
		}
		tData.setServerMode(mode);
		tData.setServiceType(serviceType);
	}

	private String getSourceType(String message) {
		HL7Message m = new HL7Message(message);
		String[] mshVals = {
				m.getField(HL7Message.SENDING_APPLICATION),
				m.getField(HL7Message.SENDING_FACILITY)
		};
		if (mshService == null) {
			return "unknown";
		}
		return mshService.getSourceType(mshVals);
	}

	protected <T> ResponseEntity<T> logEndOfRequest(Work<ResponseEntity<T>> doWork) throws Fault {
		Fault error = null;
		try {
			ResponseEntity<T> result = doWork.call();
			
			if (result != null && result.getBody() instanceof SoapMessage body) {
				catchAndKillNonTestMessages();
				logResponseMessage(body);
			}
			return result;
		} catch (Fault f) {
			error = f;
			logFault(error);
			throw f;
		} catch (Throwable e) { // NOSONAR Catch any Throwable is intentional
			error = wrapExceptionAsFault(e);
			logFault(error);
			throw e;
		}
	}

	/**
	 * Log data need from the response message
	 * @param body	The response message
	 */
	public static void logResponseMessage(SoapMessage body) {
		TransactionData tData = RequestContext.getTransactionData();
		String value = "";
		if (body instanceof HasEchoBack e) {
			value = e.getEchoBack();
			tData.setResponseEchoBack(value);
		} else if (body instanceof HasHL7Message h) {
			value = h.getHl7Message();
			tData.setResponseHL7Message(value);
		}
		tData.setResponsePayloadSize(value.length());
		tData.getServerResponse().setWs_response_message(new MessageInfo(body, EndpointType.SERVER, Direction.OUTBOUND, AppProperties.isProduction()));
	}

	/**
	 * Log data from any fault.
	 * @param fault	The fault
	 * @return	The fault
	 */
	public static Fault logFault(Fault fault) {
		if (fault instanceof HasDestinationUri dcf) {
			log.error(Markers2.append(fault, "destinationUrl", dcf.getDestinationUri()), "{} occured during processing: {}", fault.getClass().getSimpleName(), getCauseMessage(fault));
		} else {
			log.error(Markers2.append(fault), "{} occured during processing: {}", fault.getClass().getSimpleName(), getCauseMessage(fault));
		}
		TransactionData tData = RequestContext.getTransactionData();
		if (tData == null) {
			// This should never happen, protect against it and log an error.
			tData = RequestContext.init();
			log.error("LogFault called without TransactionData present");
		}
		tData.setProcessError(fault);
		return fault;
	}

	protected static String getCauseMessage(Fault fault) {
		if (fault.getCause() == null) {
			return fault.getMessage();
		}
		return fault.getCause().getMessage();
	}

	/**
	 * Wrap any unexpected exception as a fault
	 * @param fault The exception to wrap
	 * @return	The fault as an UnexpectedExceptionFault
	 */
	public static Fault wrapExceptionAsFault(Throwable fault) {
		if (fault instanceof Fault f) {
			return f;
		}
		log.error(Markers2.append(fault), "Unexpected Exception: {}", fault.getMessage());
		return new UnexpectedExceptionFault(fault, "An unexpected exception occured");
	}

	/**
	 * Report an invalid API request
	 * @param req	The request
	 * @return	The fault response
	 */
	@Operation(summary = "Controls reporting on invalid methods", hidden=true, description = "Returns a SOAP Fault for invalid methods")
	@ApiResponse(responseCode = "405", description = "An invalid method was used", content = @Content)
	@RequestMapping(produces =
			{	MediaType.TEXT_PLAIN_VALUE,
					MediaType.TEXT_XML_VALUE,
					MediaType.TEXT_HTML_VALUE, // Postel's law
					MediaType.APPLICATION_XML_VALUE,
					"application/soap",
					"application/soap+xml",
					"application/wsdl+xml"
			},
			method = { RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.PUT, RequestMethod.TRACE }
	)
	public ResponseEntity<FaultMessage> invalidRequest(HttpServletRequest req) {
		logStartOfRequest(null);
		return handleFault(new UnsupportedOperationFault("Request Method Invalid: " + req.getMethod(), null));
	}

	/**
	 * Get the WSDL
	 * @param wsdl	The WSDL parameter
	 * @param wsdl2	An alternate WSDL parameter in lowercase
	 * @param xsd	The XSD to get
	 * @param devAction	The devAction header
	 * @return	The requested WSDL or XDS in a ResponseEntity.
	 */
	@Operation(summary = "Get the description of this Interface", description = "Returns the Web Service Description Language (WSDL) or XML Schema Description (XSD) for this interface")
	@ApiResponse(responseCode = "200", description = "The WSDL or requested schema", content = @Content(mediaType = MediaType.APPLICATION_XML_VALUE, schema = @Schema(implementation = IMessageHeader.Map.class)))
	@ApiResponse(responseCode = "404", description = "The requested schema was not found", content = @Content)
	@ApiResponse(responseCode = "500", description = "A Fault occurred while processing", content = @Content)
	@GetMapping(produces =
			{	MediaType.TEXT_PLAIN_VALUE,
					MediaType.TEXT_XML_VALUE,
					MediaType.APPLICATION_XML_VALUE,
					"application/soap",
					"application/soap+xml",
					"application/wsdl+xml"
			}
	)
	public ResponseEntity<?> getWsdlOrXsd(  // NOSONAR Intentional use of ?
											@Schema(description="Get the WSDL")
											@RequestParam(name="WSDL", required=false)
											String wsdl,
											@Schema(description="Alternate request to get the WSDL")
											@RequestParam(name="wsdl", required=false)
											String wsdl2,
											@Schema(description="The Schema to get")
											@RequestParam(name="xsd", required=false)
											String xsd,
											@Schema(description="Throws the fault specified in the header parameter")
											@RequestHeader(value="X-IIS-Hub-Dev-Action", required=false)
											String devAction
	) {
		Fault fault = null;
		logStartOfRequest(null);
		try {
			wsdl = ObjectUtils.defaultIfNull(wsdl, wsdl2);
			if (wsdl == null && StringUtils.isEmpty(xsd)) {
				throw new UnsupportedOperationFault("At least one of the WSDL or xsd parameters must be present", null);
			} else if (xsd != null && XSD_FILES.contains(xsd)) {
				throw new UnsupportedOperationFault("Schema " + xsd + " unknown", null);
			}
			String resourceName = wsdl != null ? "/soap/wsdl/" + getWsdl() : ("/soap/schema" + xsd);
			return logEndOfRequest(() -> getResource(resourceName));
		} catch (Fault ex) {
			fault = ex;
		} catch (Throwable ex) {
			fault = wrapExceptionAsFault(ex);
		}
		return handleFault(fault);
	}

	/**
	 * Submit a SOAP request
	 * @param soapMessage	The request
	 * @param devAction		The devAction header (requesting an exception be thrown for development testing)
	 * @return The response
	 * @throws SecurityFault	If the message does not match acceptable test patterns in non-production environments.
	 */
	@Operation(summary = "Post a message to the SOAP Interface", description = "Send a request to the SOAP Interface for IZ Gateway")
	@ApiResponse(responseCode = "200", description = "The request completed normally", content = @Content(mediaType = MediaType.APPLICATION_XML_VALUE))
	@ApiResponse(responseCode = "500", description = "A fault occured while processing the request", content = @Content)
	@PostMapping(produces = {
		"application/soap+xml",
		"application/soap",
		MediaType.APPLICATION_XML_VALUE,
		MediaType.TEXT_XML_VALUE,
		MediaType.TEXT_PLAIN_VALUE,
		MediaType.TEXT_HTML_VALUE	// Postel's law
	})

	public ResponseEntity<?> submitSoapRequest( // NOSONAR ? is intentional here
		@RequestBody SoapMessage soapMessage,
		@Schema(description="Throws the fault specified in the header parameter")
		@RequestHeader(value="X-IIS-Hub-Dev-Action", required=false)
		String devAction
	) throws SecurityFault {
		logStartOfRequest(soapMessage);
		
		Fault fault;
		try {
			catchAndKillNonTestMessages();

			if (soapMessage instanceof SubmitSingleMessageRequest s) {
				return logEndOfRequest(() -> submitSingleMessage(s));
			} else if (soapMessage instanceof ConnectivityTestRequest c) {
				return logEndOfRequest(() -> connectivityTest(c));
			} else {
				throw new UnexpectedExceptionFault("Schema Error", "The content did not reflect the expected type. " + JsonUtils.toString(soapMessage), null, RetryStrategy.CORRECT_MESSAGE, null);
			}
		} catch (Fault ex) {
			fault = ex;
		} catch (Throwable ex) {
			fault = wrapExceptionAsFault(ex);
		}
		return handleFault(fault);
	}

	private void catchAndKillNonTestMessages() throws SecurityFault {
		if (!isCatchAndKillEnabled()) {
			return;
		}
		if (RequestContext.getTransactionData().isProd()) {
			return;
		}
		if (RequestContext.getTransactionData().isKnownTestMessage() ) {
			return;
		}
		// Message does not match known test cases.
		throw SecurityFault.generalSecurity("Unknown Test Case", null, null);
	}

	private void checkMessageSize(SoapMessage soapMessage, MessageTooLargeFault.Direction direction) throws MessageTooLargeFault {
		if (soapMessage.length() > getMaxMessageSize()) {
			throw new MessageTooLargeFault(direction, soapMessage.length(), getMaxMessageSize());
		}
	}

	protected boolean isValidMessage(SoapMessage soapMessage) {
		return getMessageNamespace().equals(soapMessage.getSchema());
	}


	protected IDestination getDestination(String destinationId) throws UnknownDestinationFault { // NOSONAR Subclass throws this fault
		return getDestinationService().getExample(destinationId);
	}

	@Operation(summary = "Post a SubmitSingleMessage request to the SOAP Interface of the destination",
			description = "Send an HL7 Message to the destination specified in SubmitSingleMessage.hubRequestHeader.destinationId")
	@ApiResponse(responseCode = "200", description = "The request completed normally", content = @Content(mediaType = MediaType.APPLICATION_XML_VALUE))
	@ApiResponse(responseCode = "500", description = "A fault occured while processing the request", content = @Content)
	@PostMapping(value = "/submitSingleMessage/{destinationId}", produces = MediaType.APPLICATION_XML_VALUE)
	@RolesAllowed({ Roles.ADMIN, Roles.INTERNAL })
	/**
	 * Default behavior of the mock except for specially crafted messages is to echo back the original message
	 *
	 * @param req	The original request.
	 * @param destinationId
	 * @return	The response.
	 */
	protected ResponseEntity<?> submitSingleMessage(  // NOSONAR ? is intentional
													  @Schema(description="The message to send")
													  @RequestBody SubmitSingleMessageRequest submitSingleMessage
	) throws Fault {
		checkMessage(submitSingleMessage);
		String ident = submitSingleMessage.findTestCaseIdentifier();
		MockMessage mock = MockMessage.getMock(ident);
		log.debug("Mock Requested: {}\t{}", ident, mock);
		if (mock != null) {
			return mock.getMessage(submitSingleMessage);
		} 
		return new ResponseEntity<>(new SubmitSingleMessageResponse(submitSingleMessage, getMessageNamespace(), true), HttpStatus.OK);
	}

	protected void checkMessage(SoapMessage soapMessage) throws MessageTooLargeFault, UnexpectedExceptionFault, SecurityFault {
		checkMessageSize(soapMessage, MessageTooLargeFault.Direction.REQUEST);

		// A "feature" of the new marshalling structure is that systems don't care WHICH version of the schema you
		// use, 2011, 2014, or Hub.  So, we check for a mismatch from the expected values.
		if (!isValidMessage(soapMessage)) {
			throw new UnexpectedExceptionFault("Schema Error", "The input does not match the requirements of the " + getMessageNamespace() + " schema.", null, RetryStrategy.CORRECT_MESSAGE, null);
		}
		if (soapMessage instanceof HasCredentials cred) {
			checkCredentials(cred);
		}
	}

	protected ResponseEntity<SoapMessage> checkResponseEntitySize(ResponseEntity<SoapMessage> message) throws MessageTooLargeFault {
		Object body = message.getBody();
		if (body instanceof SoapMessage sm) {
			checkMessageSize(sm, MessageTooLargeFault.Direction.RESPONSE);
		}
		return message;
	}

	@Operation(summary = "Post a ConnectivityTest request to the SOAP Interface of the destination", description = "Send message to be echoed back to the destination specified in ConnectivityTest.hubRequestHeader.destinationId. If destinationId is empty,"
			+ "IZ Gateway itself responds to the echoBack request.")
	@ApiResponse(responseCode = "200", description = "The request completed normally", content = @Content(mediaType = MediaType.APPLICATION_XML_VALUE))
	@ApiResponse(responseCode = "500", description = "A fault occured while processing the request", content = @Content)
	@PostMapping(value = "/connectivityTest/{destinationId}", produces = MediaType.APPLICATION_XML_VALUE)
	@RolesAllowed({ Roles.ADMIN, Roles.INTERNAL })
	protected ResponseEntity<?> connectivityTest( 	// NOSONAR ? is intentional
													 @Schema(description="The message to send.")
													 @RequestBody ConnectivityTestRequest connectivityTest
	) throws Fault {
		checkMessage(connectivityTest);
		return checkResponseEntitySize(
			new ResponseEntity<>(
				new ConnectivityTestResponse(connectivityTest, getMessageNamespace(), false),
				HttpStatus.OK
			)
		);
	}

	private ResponseEntity<String> getResource(String path) throws UnsupportedOperationFault {
		String result = resourceCache.get(path);
		if (result == null) {
			try {
				result = IOUtils.resourceToString(path, StandardCharsets.UTF_8);
				result = result.replace("{{SERVER.NAME}}", serverName);
				// Need to substitute dev.phiz-project.org in WSDL with correct hostname
				// Need to add a test on get on WSDL to verify hostname is correct
			} catch (IOException ex) {
				RequestContext.getTransactionData().setMessageType(MessageType.INVALID_REQUEST);
				throw new UnsupportedOperationFault("Resource " + path + " unknown", ex);
			}
			resourceCache.put(path, result);
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	protected ResponseEntity<FaultMessage> handleFault(Fault fault) {
		logFault(fault);
		FaultMessage faultMessage = new FaultMessage(fault, messageNamespace);
		faultMessage.updateAction(isHub());
		logResponseMessage(faultMessage);
		return new ResponseEntity<>(faultMessage, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler(SoapConversionException.class)
	protected ResponseEntity<FaultMessage> handleBadXML(SoapConversionException ex) {
		Fault f = null;
		
		logStartOfRequest(null);
		if (ex.getCause() instanceof SoapParseException) {
			f = new UnexpectedExceptionFault("Syntax Error", null, ex, RetryStrategy.CORRECT_MESSAGE, "An exception occurred parsing the SOAP Message");
		} else if (ex.getCause() instanceof SecurityFault sf) {
			f = sf;
		} else {
			f = new UnexpectedExceptionFault("Syntax Error", null, ex, RetryStrategy.CORRECT_MESSAGE, "An exception occurred processing the SOAP Message");
		}
		return handleFault(f);
	}
}
