package gov.cdc.izgateway.xform.endpoints.fhir;

import gov.cdc.izgateway.security.AccessControlRegistry;
import gov.cdc.izgw.v2tofhir.converter.MessageParser;
import gov.cdc.izgw.v2tofhir.datatype.HumanNameParser;
import gov.cdc.izgw.v2tofhir.segment.PIDParser;
import gov.cdc.izgw.v2tofhir.utils.QBPUtils;
import gov.cdc.izgw.v2tofhir.utils.ContentUtils;
import gov.cdc.izgw.v2tofhir.utils.FhirIdCodec;
import gov.cdc.izgw.v2tofhir.utils.IzQuery;
import gov.cdc.izgw.v2tofhir.utils.ParserUtils;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IllegalFormatCodePointException;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.SearchEntryMode;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.ImmunizationRecommendation;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StringType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.v251.message.QBP_Q11;
import ca.uhn.hl7v2.model.v251.segment.MSH;
import gov.cdc.izgateway.common.HasDestinationUri;
import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.logging.info.SourceInfo;
import gov.cdc.izgateway.logging.markers.Markers2;
import gov.cdc.izgateway.model.RetryStrategy;
import gov.cdc.izgateway.security.AccessControlValve;
import gov.cdc.izgateway.soap.fault.SecurityFault;
import gov.cdc.izgateway.soap.fault.UnexpectedExceptionFault;
import gov.cdc.izgateway.soap.message.FaultMessage;
import gov.cdc.izgateway.soap.message.SoapMessage;
import gov.cdc.izgateway.soap.message.SubmitSingleMessageRequest;
import gov.cdc.izgateway.soap.message.SubmitSingleMessageResponse;
import gov.cdc.izgateway.soap.message.WsaHeaders;
import gov.cdc.izgateway.xform.endpoints.hub.HubController;
import gov.cdc.izgateway.xform.security.Roles;
import io.azam.ulidj.ULID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * The FHIR Controller implements methods enabling users to query an IIS via FHIR instead of
 * V2 Messaging.
 * 
 * The base capability of this component is to convert a FHIR query into a Z34 or Z44 query
 * depending on the resource requested.
 * 
 * This is the special sauce of the V2 to FHIR Converter for Immunization Registries
 * 
 * Each Patient known to an IIS has a "chart", that is reflected by their immunization history.
 * The operating assumption is that the Patient/id of the FHIR resource can be converted back to the parameters necessary
 * to query the IIS when necessary to collect the patient chart. Given a patient id, the chart can be reconstructed, and 
 * requested resources can be read back from the chart.  So long as the IIS retains the data, the resource should be able
 * to be "found" again.
 * 
 * The resources supported are listed below.  Values between () indicate source
 * of the resource data.  Values between [] indicate how it is uniquely identified in the chart.
 * 
 * - Patient (PID+PD1) [name,gender,dob,first identifier]
 * - Immunization (ORC/RXA/RXR+OBX)  [ServiceRequest.identifer from ORC-3]
 * - ImmunizationRecommendation* (ORC/RXA/RXR+OBX) [ServiceRequest.identifer from ORC-3]  
 * - ServiceRequest (ORC) [ORC-3]
 * - Organization (MSH, ORC-23, PD1-4)    [name, identifier]
 * - Endpoint (MSH) [name]
 * - Practitioner (EVN-5, OBX-15,16,25, ORC-12,21, PD1-4, PV1-7,8,9,17,52, RXA-10) [name, identifier]
 * - PractitionerRole (OBX-15,16,25, ORC-12,21,23) [org unique value + practitioner unique value]
 * - Location (EVN-7, ORC-29, PV1-3,6,11,37,40,42,43, RXA-11,27,28) [name computed from location]
 * - RelatedPerson (NK1) [name]
 * - Encounter (PV1) [first identifier]
 * - Account (PID-18) [identifier]
 * 
 * * ImmunizationRecommendation resources are notable "transient" in nature and change over time, and so may not be retrievable
 *   at a future point in time.
 * 
 * @author Audacious Inquiry 
 */
@RestController
@RolesAllowed({Roles.XFORM_SENDING_SYSTEM, Roles.ADMIN})
@RequestMapping("/fhir")
@Slf4j
@Lazy(false)
public class FhirController {

    private static final String PATIENT = "Patient";

    /**
     * Configuration of the V2toFHIR Conversion
     * @author Audacious Inquiry
     */
    @Configuration
    @ConfigurationProperties(prefix = "v2tofhir")
    @Data
    public static class FhirConfiguration {
        /** The sending application value to use in the HL7 V2 message */
        private String sendingApplication;
        /** The sending facility value to use in the HL7 V2 message */
        private String sendingFacility;
        /** The receiving application value to use in the HL7 V2 message */
        private String receivingApplication;
        /** The receiving facility value to use in the HL7 V2 message */
        private String receivingFacility;
        /** The facility id value to use in the IZG Hub Message */
        private String facilityId;
    }
    
    private final FhirConfiguration config;
    private final HubController hub;

    /**
     * Construct the FhirController.  It calls HubController methods directly so
     * needs to know where the hub is.
     * @param hub    The hub controller to talk to.
     * @param config The configuration for the converter
     * @param registry The Access Control Registry
     */
    public FhirController(@Autowired HubController hub, FhirConfiguration config, AccessControlRegistry registry) {
        this.hub = hub;
        this.config = config;
        registry.register(this);
    }
    
    /**
     * Send a Z34 Request Immunization History or Z44 Request Evaluated History and Recommendation 
     * request to an IIS and return the requested resources.
     * 
     * @param destinationId    The destination for the FHIR Request.  This takes the place of the
     * destinationId element shown below in Soap Messages.
     * 
     * <pre xmlns="urn:cdc:iisb:hub:2014">
     *      <HubRequestHeader>
     *     <DestinationId>dev</DestinationId>
     *   </HubRequestHeader>
     * </pre>
     * 
     * @param req    The HttpServletRequest to get the request parameters from.
     * @return    A FHIR Bundle containing the search results, or an OperationOutcome resource if there
     * was an exception, fault or error processing the message.
     * @throws FaultException When a SoapFault is reported.
     * @throws HL7Exception When a message cannot be parsed.
     * @throws UnexpectedException When something goes wrong that shouldn't have
     * @throws SecurityFault When a security fault occurs
     */
    @Operation(
        summary = "Request an Immunization History or Immunization Recommendation via FHIR",
        description = "Send a request to the FHIR Interface for IZ Gateway Xform Service"
    )
    @ApiResponse(
        responseCode = "200",
        description = "The request completed normally",
        content = {
            @Content(mediaType = "application/json"),
            @Content(mediaType = "application/xml"),
            @Content(mediaType = "application/yml")
        }
    )
    @ApiResponse(
        responseCode = "400",
        description = "An error occured while processing the request",
        content = {@Content}
    )
    @ApiResponse(
        responseCode = "500",
        description = "An internal error occured while processing the request",
        content = {@Content}
    )
    @RequestMapping(
    	value = { 
            "/{destinationId}/Immunization", 
            "/{destinationId}/ImmunizationRecommendation",
            "/{destinationId}/Patient"
        },
        method = { 
            RequestMethod.GET,    // Typical web based query 
            RequestMethod.POST, // Safer because POST parameters don't wind up in access logs
            RequestMethod.HEAD    // Used with SMART and other auth mechanisms.
        },
        produces = {
            "application/fhir+xml", 
            "application/fhir+json", 
            "application/fhir+yaml",
            "application/xml",
            "application/json", 
            "application/yaml",
            "text/xml"
        }
    )
    public ResponseEntity<Bundle> iisQuery(
        @PathVariable String destinationId,
        HttpServletRequest req
    ) throws FaultException, HL7Exception, UnexpectedException, SecurityFault {
    	String summary = req.getParameter("_summary");
    	String count = req.getParameter("_count");
    	if (Arrays.asList("summary", "count").contains(summary)) {
    		return connectionTest(count);
    	}
        return processQuery(req, destinationId);
    }
    
    /**
     * This exists to allow query connector to validate authentication
     * parameters when connecting via /Patients?_summary=count&_count=1
     * @param count	Not used
     * @return	A SearchSet Bundle reporting 100 patients available.
     */
    private ResponseEntity<Bundle> connectionTest(String count) {
		Bundle b = new Bundle();
		b.setId(new IdType(b.fhirType() + "/" + ULID.random()));
		b.setType(BundleType.SEARCHSET);
		b.setTotal(100);
		return new ResponseEntity<>(b, HttpStatus.OK);
    }

    /**
     * Read an Immunization, ImmunizationRecommendation or Patient resource.  This just does a query by _id on patient,
     * and then selects the appropriate result.
     * 
     * @param destinationId    The identifier of the destination IIS
     * @param id The identifier of the immunization resource.
     * @param req    The HttpServletRequest so we can process parameters.
     * @return    The requested Resource
     * @throws FaultException    When a fault occurs.
     * @throws HL7Exception    When an HL7 Message exception occurs
     * @throws UnexpectedException When some other exception occurs
     * @throws SecurityFault When a security fault occurs
     */
    @Operation(
            summary = "Read an Immunization, ImmunizationRecommendation or Patient resource via FHIR",
            description = "Translate the read into a query and return the requested resource"
        )
    @ApiResponse(
        responseCode = "200",
        description = "The request completed normally",
        content = {
            @Content(mediaType = "application/json"),
            @Content(mediaType = "application/xml"),
            @Content(mediaType = "application/yml")
        }
    )
    @ApiResponse(
        responseCode = "404",
        description = "The requested resource could not be found",
        content = {@Content}
    )
    @ApiResponse(
        responseCode = "500",
        description = "An internal error occured while processing the request",
        content = {@Content}
    )
    @RequestMapping(
    	value= { 
            "/{destinationId}/Immunization/{id}", 
            "/{destinationId}/ImmunizationRecommendation/{id}", 
            "/{destinationId}/Patient/{id}", 
        },
        method = { 
            RequestMethod.GET,    // Typical web based query 
            RequestMethod.HEAD    // Used with SMART and other auth mechanisms.
        },
        produces = {
            "application/fhir+xml", 
            "application/fhir+json", 
            "application/fhir+yaml",
            "application/xml",
            "application/json", 
            "application/yaml",
            "text/xml"
        }
    )

    public ResponseEntity<Resource> iisRead(
        @PathVariable String destinationId,
        @PathVariable String id,
        HttpServletRequest req
    ) throws FaultException, HL7Exception, UnexpectedException, SecurityFault {
        String decodedId = null;
        try {
            decodedId = FhirIdCodec.decode(id);
        } catch (IllegalFormatCodePointException ex) {
            return notFound(id);
        }
        String resourceType = StringUtils.substringBetween(req.getRequestURI(), destinationId + "/", "/" + id); 
        
        String[] idParts = decodedId.split("\\|");
        boolean isPatient = PATIENT.equals(resourceType); 
        int index = isPatient ? 0 : 2;
        if (idParts.length < index + 2) {
            return notFound(id);
        }
        Identifier ident = new Identifier().setSystem(idParts[index]).setValue(idParts[index + 1]);
        
        RequestWithModifiableParameters wrapper = new RequestWithModifiableParameters(req);
        wrapper.addParameter(isPatient ? Patient.SP_IDENTIFIER : IzQuery.PATIENT_LIST, idParts[0] + "|" + idParts[1]);
        ResponseEntity<Bundle> res = processQuery(wrapper, destinationId);
        Bundle b = res.getBody();
        if (b == null) {
            // Technically this is an error.
            return notFound(id);
        }
        Resource resource = b.getEntry().stream()
            .map(e -> e.getResource())
            .filter(r -> resourceType.equals(r.fhirType()) && ident.equalsShallow(getIdentifier(r)))
            .findFirst()
            .orElse(null);
        
        if (resource == null) {
            return notFound(id);
        }
        return new ResponseEntity<>(resource, res.getHeaders(), HttpStatus.OK);
    }
    
    /**
	 * Perform the patient match operation. This performs a query on patient, and then selects 
	 * the appropriate result.
	 * 
	 * @param destinationId    The identifier of the destination IIS
	 * @param body    The operation parameters, as a Parameters resource, or a Patient resource
	 * @param req    The HttpServletRequest so we can process parameters.
	 * @return    A bundle of Patient Resources
	 * @throws FaultException    When a fault occurs.
	 * @throws HL7Exception    When an HL7 Message exception occurs
	 * @throws UnexpectedException When some other exception occurs
	 * @throws SecurityFault When a security fault occurs
	 */
	@Operation(
	        summary = "Perform the Patient/$match operation",
	        description = "Translate the operation into a query and return the requested resources"
	    )
	@ApiResponse(
	    responseCode = "200",
	    description = "The request completed normally",
	    content = { @Content(mediaType = "application/json")}
	)
	@ApiResponse(
	    responseCode = "400",
	    description = "The request was invalid",
	    content = {@Content}
	)
	@ApiResponse(
	    responseCode = "500",
	    description = "An internal error occured while processing the request",
	    content = {@Content}
	)
	@RequestMapping(value= "/{destinationId}/Patient/$match", 
	    method = { 
	        RequestMethod.POST,    // Typical web based query 
	        RequestMethod.HEAD    // Used with SMART and other auth mechanisms.
	    },
	    produces = {
	        "application/yaml",
	        "application/json", 
	        "application/xml",
	        "application/fhir+xml", 
	        "application/fhir+json", 
	        "application/fhir+yaml",
	        "text/xml"
	    }
	)
    public ResponseEntity<Resource> iisPatientMatch(
        @PathVariable String destinationId,
        @RequestBody Resource body,
        HttpServletRequest req
    ) throws FaultException, HL7Exception, UnexpectedException, SecurityFault {
        IDIMatch.PatientMatchParameters match = new IDIMatch.PatientMatchParameters();
        
        if ("Parameters".equals(body.fhirType())) {
            Parameters params = (Parameters)body;
            String message = null; 
            try {
                message = "Parameters.resource must contain a Patient resource";
                match.setSearchPatient((Patient)params.getParameter("resource").getResource());
                
                message = "Parameters.onlySingleMatch must contain a Boolean value";
                ParametersParameterComponent param = params.getParameter("onlySingleMatch");
                if (param != null) {
                	match.setOnlySingleMatch(((BooleanType)param.getValue()).booleanValue());
                }

                message = "Parameters.onlyCertainMatches must contain a Boolean value";
                param = params.getParameter("onlyCertainMatches");
                if (param != null) {
                	match.setOnlyCertainMatches(((BooleanType)param.getValue()).booleanValue());
                }

                message = "Parameters.count must contain an Integer value between 1 and 10";
                param = params.getParameter("count");
                if (param != null) { 
                	match.setCount(((IntegerType)param.getValue()).getValue());
                    if (match.getCount() < 1 || match.getCount() > 10) {
                        return illegalArguments(message);
                    }
                }
            } catch (ClassCastException ex) {
                return illegalArguments(message);
            }
        } else if (PATIENT.equals(body.fhirType())) {
            match.setSearchPatient((Patient) body);
        } else {
            return illegalArguments("Body invalid, expected Patient or Parameters");
        }
        
        
        RequestWithModifiableParameters wrapper = new RequestWithModifiableParameters(req);
        wrapper.resetParameters();
        wrapper.addParameter(IzQuery.COUNT, Integer.toString(match.getCount()));
        setParameters(wrapper, match.getSearchPatient());
        
        Bundle b = this.processQuery(wrapper, destinationId).getBody();
        
        IDIMatch.score(b, match);
        
        return new ResponseEntity<>(b, HttpStatus.OK);
    }
    
    /**
     * Set the search parameters for the patient using the input searchPatient
     * as an example.
     * 
     * @param wrapper    The request to set the parameters for 
     * @param patient    The patient to be searched for.
     */
    private static void setParameters(RequestWithModifiableParameters wrapper, Patient patient) {
        if (patient.hasIdentifier()) {
            for (Identifier identifier: patient.getIdentifier()) {
                TokenParam t = new TokenParam();
                t.setSystem(identifier.getSystem());
                t.setValue(identifier.getValue());
                wrapper.addParameter(Patient.SP_IDENTIFIER, t.getValueAsQueryToken(null));
            }
        }
        setNameParameters(wrapper, patient);
        setBirthDateParameters(wrapper, patient);
        if (patient.hasGender()) {
            wrapper.addParameter(Patient.SP_GENDER, patient.getGender().toString());
        }
        setAddressParameters(wrapper, patient);
    }

    private static void setNameParameters(RequestWithModifiableParameters wrapper, Patient patient) {
        if (patient.hasName()) {
            HumanName name = patient.getNameFirstRep();
            if (name.hasText() && !name.hasFamily() && !name.hasGiven()) {
                name = HumanNameParser.computeFromString(name.getText());
            }
            if (name.hasFamily()) {
                wrapper.addParameter(Patient.SP_FAMILY, name.getFamily());
            }
            if (name.hasGiven()) {
                for (StringType given: name.getGiven()) {
                    wrapper.addParameter(Patient.SP_GIVEN, given.asStringValue());
                }
            }
            if (name.hasSuffix()) {
                wrapper.addParameter("suffix", name.getSuffixAsSingleString());
            }
        }
        if (patient.hasExtension(PIDParser.MOTHERS_MAIDEN_NAME)) {
            Extension maidenName = patient.getExtensionByUrl(PIDParser.MOTHERS_MAIDEN_NAME);
            wrapper.addParameter("mothers-maiden-name", maidenName.getValueAsPrimitive().getValueAsString());
        }
    }


    private static void setBirthDateParameters(RequestWithModifiableParameters wrapper, Patient patient) {
        if (patient.hasBirthDate()) {
            DateParam birthDate = new DateParam();
            birthDate.setValue(patient.getBirthDate());
            String value = StringUtils.substringBefore(birthDate.getValueAsQueryToken(null), "T");
            wrapper.addParameter(Patient.SP_BIRTHDATE, value);
        }
        if (patient.hasMultipleBirth()) {
            PrimitiveType<?> pt = (PrimitiveType<?>) patient.getMultipleBirth();
            if (pt instanceof BooleanType) {
                wrapper.addParameter("multipleBirth-indicator", pt.asStringValue());
            } else if (pt instanceof IntegerType) {
                wrapper.addParameter("multipleBirth-order", pt.asStringValue());
            }
        }
    }

    private static void setAddressParameters(RequestWithModifiableParameters wrapper, Patient patient) {
        if (patient.hasAddress()) {
            Address address = patient.getAddressFirstRep();
            if (address.hasLine()) {
                for (StringType line : address.getLine()) {
                    wrapper.addParameter(Patient.SP_ADDRESS, line.asStringValue());
                }
            }
            if (address.hasCity()) {
                wrapper.addParameter(Patient.SP_ADDRESS_CITY, address.getCity());
            }
            if (address.hasState()) {
                wrapper.addParameter(Patient.SP_ADDRESS_STATE, address.getState());
            }
            if (address.hasPostalCode()) {
                wrapper.addParameter(Patient.SP_ADDRESS_POSTALCODE, address.getPostalCode());
            }
            if (address.hasCountry()) {
                wrapper.addParameter(Patient.SP_ADDRESS_COUNTRY, address.getCountry());
            }
        }
    }
    
    private ResponseEntity<Resource> notFound(String id) {
        OperationOutcome oo = new OperationOutcome();
        OperationOutcomeIssueComponent issue = oo.addIssue();
        issue.setCode(IssueType.NOTFOUND);
        issue.setSeverity(IssueSeverity.ERROR);
        issue.setDiagnostics("Resource not found " + id);
        return new ResponseEntity<>(oo, HttpStatus.NOT_FOUND);
    }
    
    private ResponseEntity<Resource> illegalArguments(String message) {
        OperationOutcome oo = new OperationOutcome();
        OperationOutcomeIssueComponent issue = oo.addIssue();
        issue.setCode(IssueType.INVALID);
        issue.setSeverity(IssueSeverity.ERROR);
        issue.setDiagnostics(message);
        return new ResponseEntity<>(oo, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Generate an Immunization query to an IIS and convert the result to FHIR, returning
     * the requested information.
     * @throws SecurityFault 
     */
    private ResponseEntity<Bundle> processQuery(
        HttpServletRequest req, 
        String destinationId
    ) throws FaultException, HL7Exception, UnexpectedException, SecurityFault {
        String queryType = StringUtils.contains(req.getRequestURI(), "ImmunizationRecommendation") ? IzQuery.RECOMMENDATION : IzQuery.HISTORY;
        
        // Create the request and set the destination
        SubmitSingleMessageRequest request = new SubmitSingleMessageRequest();
        request.setSchema(SoapMessage.HUB_NS);
        request.getHubHeader().setDestinationId(destinationId);
        request.setFacilityID(config.getFacilityId());
        
        SourceInfo source = RequestContext.getSourceInfo();
        if (StringUtils.isEmpty(source.getCommonName()) && AccessControlValve.isLocalHost(source.getIpAddress())) {
            source.setCommonName("localhost");
        }
        SubmitSingleMessageResponse resp = null;
        try {
            // Create the message structure
            QBP_Q11 qbp = QBPUtils.createMessage(queryType);
            
            setMSHValues(qbp);
            
            boolean isPatient = StringUtils.contains(req.getRequestURI(), "/Patient");

    		@SuppressWarnings("unused")
    		IzQuery query = setQueryParameters(req, qbp, isPatient);
            
            // Set the message content
            request.setHl7Message(qbp.encode());
            setWsaHeaders(request, qbp);
            
            // Submit the message. 
            ResponseEntity<?> entity = hub.submitSoapRequest(request, null);
            
            // Return the response
            if (entity.getBody() instanceof FaultMessage fault) {
                throw new FaultException(fault);
            }
            
            if (entity.getBody() instanceof SubmitSingleMessageResponse response) {
                resp = response;
                Bundle b = convertResponseToFHIR(response);
                adjustIdentifiers(b);
                filter(b, req);
                return new ResponseEntity<>(b, ContentUtils.getHeaders(req), HttpStatus.OK);
            }
            
            throw new ServiceConfigurationError(
                "The Soap Response was neither SubmitSingleMessageResponse nor FaultMessage");
        } catch (HL7Exception e) {
            log.error(Markers2.append(e), "{} creating message for {}: {}", 
                e.getClass().getSimpleName(), destinationId, resp.getHl7Message());
            throw e;
        } catch (IllegalArgumentException argEx) {
            log.error(Markers2.append(argEx), 
                "Illegal argument: {}", argEx.getMessage());
            throw argEx;
        } catch (RuntimeException rex) {
            log.error(Markers2.append(rex), "Unexpected {}: {}", 
                rex.getClass().getSimpleName(), rex.getMessage());
            throw new UnexpectedException(rex);
        }
    }

	private IzQuery setQueryParameters(HttpServletRequest req, QBP_Q11 qbp, boolean isPatient) throws HL7Exception {
		IzQuery query;
		// Add request parameters to the QPD Segment
		if (req instanceof RequestWithModifiableParameters reqp) {
		    query = QBPUtils.addParamsToQPD(qbp, reqp.getParameters(), isPatient);  // NOSONAR query is for debugging
		} else {
		    query = QBPUtils.addRequestParamsToQPD(qbp, req.getParameterMap(), isPatient); // NOSONAR query is for debugging
		}
		return query;
	}

	private void setMSHValues(QBP_Q11 qbp) throws DataTypeException {
		/* These value should be adjusted in the pipeline configuration once the message has been forwarded. */
		QBPUtils.setSendingApplication(qbp, RequestContext.getPrincipal().getName());
		QBPUtils.setSendingFacility(qbp, RequestContext.getPrincipal().getOrganization());
		QBPUtils.setReceivingApplication(qbp, config.getReceivingApplication());
		QBPUtils.setReceivingFacility(qbp, config.getReceivingFacility());
		// These are fixed values that should normally be used for a query message
		MSH msh = qbp.getMSH();
		// Some destinations may require a value of "T" in the onboarding environment, but
		// all use "P" in production, and most use "P" elsewhere. Set the most useful value
		// understanding that this can be adjused by the transformation service.
		msh.getProcessingID().getProcessingID().setValue("P");
		// These are appropriate values for any endpoint connecting to IZGW
		msh.getApplicationAcknowledgmentType().setValue("AL");
		msh.getAcceptAcknowledgmentType().setValue("ER");
	}
    
    /**
     * Create the response
     * @param qbp    The original QBP Request
     * @param response    The response to the SOAP Request
     * @return    The Bundle wrapped in a response entity.
     * @throws HL7Exception    If any errors occurred while parsing the HL7 Message.
     * 
     */
    private Bundle convertResponseToFHIR(SubmitSingleMessageResponse response) throws HL7Exception {
        String hl7Message = response.getHl7Message();
        MessageParser mp = new MessageParser();
        return mp.convert(hl7Message);
    }

    /**
     * Filter the response bundle to include only those resources which were requested.
     * 
     * @param bundle    The bundle to filter
     * @param include    The include parameters
     * @param revInclude    The reverse include parameters
     * @return    The filtered bundle (NOTE: it is filtered in place).
     */
    private Bundle filter(Bundle bundle, HttpServletRequest req) {
        List<Include> includes = toIncludeList(req.getParameterValues("_include"));
        List<Include> revIncludes = toIncludeList(req.getParameterValues("_revinclude"));
        // Only include resources that were included via _include, or which reference an included resource via _revinclude.
        
        // Update the bundle type to searchset from message.
        bundle.setType(BundleType.SEARCHSET);
        String requested = StringUtils.substringAfterLast(req.getRequestURI(), "/");
        if ("$match".equals(requested)) {
            requested = PATIENT;
        }

        List<Resource> resources = new ArrayList<>();
        preFilter(bundle, includes, revIncludes, requested, resources);
        
        markIncludedResources(includes, revIncludes, resources);
        
        cleanupBundleOfUnmarkedResources(bundle);
        return bundle;
    }

    private void preFilter(Bundle bundle, List<Include> includes, List<Include> revIncludes, String requested,
            List<Resource> resources) {
        Iterator<BundleEntryComponent> it = bundle.getEntry().iterator();
        while (it.hasNext()) {
            BundleEntryComponent entry = it.next();
            Resource r = entry.getResource();
            if (r != null && r.fhirType().equals(requested)) {
                entry.getSearch().setMode(SearchEntryMode.MATCH);
                if (!resources.contains(r)) {
                    resources.add(r);
                }
            } else if (r instanceof OperationOutcome) {
                entry.getSearch().setMode(SearchEntryMode.OUTCOME);
                if (!resources.contains(r)) {
                    resources.add(r);
                }
            } else {
                removeInfrastructureCreatedResources(resources, includes, revIncludes, it, r);
            }
        }
    }
    
    private void removeInfrastructureCreatedResources(List<Resource> resources, List<Include> includes, List<Include> revIncludes,
            Iterator<BundleEntryComponent> it, Resource r) {
        if (r != null && r.getUserData(MessageParser.SOURCE) != null) {
            // Some DatatypeConverter and MessageParser created resources have limited utility.  
            // What we should we do with those depends on what resources the
            // user asks to include.  These infrastructure crafted resources can be white-listed
            // using the include or revinclude parameters.
            
            // Users can white-list these resources with the following _include parameters:
            // All:
            // _include=Resource:source:*
            // DatatypeConverter created Organization/Practitioner/RelatedPerson/Location
            // _include=Resource:source:Organization
            // MessageParser created DocumentReference/Provenance
            // _include=Resource:source:DocumentReference
            String source = r.getUserData(MessageParser.SOURCE).toString();
            if (
                // ANY Source requested
                // DatatypeConverter created resources including Organization, Practitioner, RelatedPerson, and Location
                // MessageParser created resources including DocumentReference and Provenance
                matchesSource(includes, r.fhirType())
            ) {
                // Explicitly mark these as included resources.
                r.setUserData(SearchEntryMode.class.getName(), SearchEntryMode.INCLUDE);
                resources.add(r);
                return;
            }
            
            // If users reverse include provenance, don't delete the MessageParser crafted Provenance resources.
            if (source.equals(MessageParser.class.getName()) 
            	&& revIncludes.stream().anyMatch(rinc -> "Provenance".equals(rinc.getParamType()))) {
                // Let normal include handling mark Provenance reverse includes.
                return;
            }
            // Remove classes crafted by the infrastructure as being generally not useful because
            // the enriched reference (name and identifier) is enough for production use.
            it.remove();
        }
    }

    private boolean matchesSource(List<Include> includes, String target) {
        for (Include include: includes) {
            if ("Resource".equals(include.getParamType()) 
            	&& MessageParser.SOURCE.equals(include.getParamName())
            	&& Arrays.asList("*", target, null).contains(include.getParamTargetType())
            ) {
                return true;
            }
        }
        return false;
    }

    private void markIncludedResources(List<Include> includes, List<Include> revIncludes, List<Resource> resources) {
        // For each non OperationOutcome resource in the output (which can grow
        // at each iteration of the loop, find the essential to include resources.
        for (int i = 0; i < resources.size(); i++) {
            Resource r = resources.get(i);
            if (r instanceof OperationOutcome) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Set<Reference> refs = (Set<Reference>) r.getUserData("References");
            checkReferences(includes, resources, r, refs, false);
            // For each reverse include
            @SuppressWarnings("unchecked")
            Set<Reference> revs = (Set<Reference>) r.getUserData("Reverses");
            checkReferences(revIncludes, resources, r, revs, true);
        }
    }

    private void cleanupBundleOfUnmarkedResources(Bundle bundle) {
        Iterator<BundleEntryComponent> it = bundle.getEntry().iterator();
        while (it.hasNext()) {
            BundleEntryComponent entry = it.next();
            Resource res = entry.getResource();
            if (res != null && entry.getSearch().getMode() == null) {
                SearchEntryMode mode = (SearchEntryMode) res.getUserData(SearchEntryMode.class.getName());
                if (mode != null) {
                    entry.getSearch().setMode(mode);
                }
            }
            if (entry.getSearch().getMode() == null) {
                it.remove();
            }
        }
    }

    private void checkReferences(List<Include> includes, List<Resource> resources, Resource r, Set<Reference> refs, boolean reverse) {
        if (refs != null) {
            // For each reference that resource has
            for (Reference ref: refs) {
                // For each forward include, e.g., _include=Immunization:patient:Patient
                for (Include include: includes) {
                    if (includeMatches(include, r, ref, reverse)) {
                        Resource target = (Resource) ref.getUserData("Resource");
                        if (!resources.contains(target)) {
                            resources.add(target);
                        }
                        target.setUserData(SearchEntryMode.class.getName(), SearchEntryMode.MATCH);
                    }
                }
            }
        }
    }
    
    private boolean includeMatches(Include include, Resource r, Reference ref, boolean reverse) {
        
        String type = reverse ? include.getParamTargetType() : include.getParamType();
        if (isNotAny(type) && !type.equals(r.fhirType())) {
            return false;     // not a match.
        }
        String search = include.getParamName();
        String searchNameString = ref.getUserString(reverse ? ParserUtils.REVERSE_NAMES : ParserUtils.SEARCH_NAMES);
        
        List<String> searchNames = Arrays.asList(StringUtils.split(searchNameString, ","));
        if (isNotAny(search) && !searchNames.contains(search)) {
            return false;     // not a match.
        }
        
        String target = reverse ? include.getParamType() : include.getParamTargetType();
        
        return !isNotAny(target) || target.equals(ref.getReferenceElement().getResourceType());
    }
    
    private static boolean isNotAny(String includeValue) {
    	return includeValue != null && !"*".equals(includeValue);
    }

    private List<Include> toIncludeList(String... inc) {
        if (inc == null || inc.length == 0) {
            return Collections.emptyList();
        }
        List<Include> l = new ArrayList<>();
        for (String i: inc) {
            i = normalizeInclude(i);
            Include include = new Include(i);
            l.add(include);
        }
        return l;
    }

    private static String normalizeInclude(String i) {
        // normalize missing include values *
        switch (StringUtils.countMatches(i, ':')) {
        case 0:
            i += ":*:*";
            break;
        case 1:
            i += ":*";
            break;
        default:
            break;
        }
        if (i.contains("::")) {
            i = i.replace("::", ":*");
        }
        return i;
    }

    /**
     * Adjust the identifers of the resources returned in the bundle.
     * 
     * @param b    The bundle
     * @return    The bundle
     */
    private Bundle adjustIdentifiers(Bundle b) {
        String lastPatientValue = null;
        for (BundleEntryComponent entry : b.getEntry()) {
            Resource r = entry.getResource();
            Identifier ident = getIdentifier(r);
            if (ident == null) {
                continue;
            }
            String value = ident.getSystem() + "|" + ident.getValue();
            if (r instanceof Patient) {
                lastPatientValue = value;    // NOSONAR, lastPatientValue will be used on next iteration
            } else {
                value = lastPatientValue + "|" + value;
            }
            String encoded = FhirIdCodec.encode(value);
            
            if (encoded.length() > 64) {
                // Technically, FHIR ids have to be shorter than 64 characters.  We'll ignore that, but log the issue.
                log.warn("Id too long");
            }
            IdType x = r.getIdElement();
            x.setParts(x.getBaseUrl(), x.getResourceType(), encoded, x.getVersionIdPart());
            // Correct the singular reference to this resource used in all places where
            // a reference is needed to it.
            Reference ref = (Reference) r.getUserData("Reference");
            if (ref != null) {
                x = (IdType) ref.getReferenceElement();
                x.setParts(x.getBaseUrl(), x.getResourceType(), encoded, x.getVersionIdPart());
                ref.setReferenceElement(x);
            }
        }
        return b;
    }

    private Identifier getIdentifier(Resource r) {
        Identifier ident = null;
        if (r instanceof Patient p) {
            // Immunization messages must have at least ONE patient identifier.
            ident = p.getIdentifierFirstRep();
        } else if (r instanceof Immunization iz) {
            ident = iz.getIdentifierFirstRep();
        } else if (r instanceof ImmunizationRecommendation izr) {
            ident = izr.getIdentifierFirstRep();
        }
        return ident;
    }

    private void setWsaHeaders(SubmitSingleMessageRequest request, QBP_Q11 qbp) {
        // Set a message ID for the message we are crafting.
        WsaHeaders headers = request.getWsaHeaders();
        headers.setMessageID(qbp.getMSH().getMessageControlID().getValue());
        headers.setAction("urn:cdc:iisb:hub:2014:IISHubPortType:SubmitSingleMessageRequest");
        headers.setTo("http://www.w3.org/2005/08/addressing/anonymous");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<OperationOutcome> handleException(HttpServletRequest req, IllegalArgumentException iex) {
        OperationOutcome oo = new OperationOutcome();
        oo.addIssue()
            .setCode(IssueType.INVALID)
            .setSeverity(IssueSeverity.ERROR)
            .addExpression(null)
            .setDiagnostics(iex.getMessage());
        
        return new ResponseEntity<>(oo, ContentUtils.getHeaders(req), HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(HL7Exception.class)
    ResponseEntity<OperationOutcome> handleException(HttpServletRequest req, HL7Exception hex) {
        OperationOutcome oo = new OperationOutcome();
        oo.addIssue()
            .setCode(IssueType.INVALID)
            .setSeverity(IssueSeverity.FATAL)
            .addExpression(null)
            .setDiagnostics(hex.getMessage());  
        return new ResponseEntity<>(oo, ContentUtils.getHeaders(req), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(FaultException.class)
    ResponseEntity<OperationOutcome> handleException(HttpServletRequest req, FaultException fex) {
        OperationOutcome oo = new OperationOutcome();
        FaultMessage fault = fex.getFault();

        OperationOutcomeIssueComponent issue = oo.addIssue()
            .setCode(IssueType.INVALID)
            .setSeverity(IssueSeverity.ERROR)
            .setDiagnostics(fault.getDiagnostics());
        if (fault.getFault() instanceof UnexpectedExceptionFault uex) {
            issue.setDetails(
                new CodeableConcept()
                    .setText(uex.getDiagnostics())
                    .addCoding(new Coding(fault.getSchema() + ":Fault", fault.getFaultName(), null)
                )
            );
        } else {
            issue.setDetails(
                new CodeableConcept()
                    .setText(fex.getMessage())
                    .addCoding(new Coding(fault.getSchema() + ":Fault:" + fault.getFaultName(), fault.getCode(), null)
                )
            );
        }
        
        if (fault.getFault() instanceof HasDestinationUri huri) {
            issue.addExpression(huri.getDestinationUri());
        }
        
        // Set the retry coding value
        RetryStrategy retry = setRetryCoding(issue, fex.getRetryCoding());
        
        // Set event identifier
        fex.setEventId(issue);
        // Give them the Original text of the fault if it is present.
        fex.setOriginalText(issue);
        
        return new ResponseEntity<>(oo, ContentUtils.getHeaders(req), retry != null ? retry.getStatus() : HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UnexpectedException.class)
    ResponseEntity<OperationOutcome> handleException(HttpServletRequest req, UnexpectedException uex) {
        OperationOutcome oo = new OperationOutcome();
        Class<? extends Throwable> throwable = uex.getCause().getClass();
        String system = "urn:java:package:" + throwable.getPackageName();
        String code = throwable.getSimpleName(); 
        OperationOutcomeIssueComponent issue = oo.addIssue()
            .setCode(IssueType.EXCEPTION)
            .setSeverity(IssueSeverity.FATAL)
            .setDetails(new CodeableConcept()
                .addCoding(new Coding(system, code, null))
            ).setDiagnostics(uex.getCause().getMessage());
        setRetryCoding(issue, RetryStrategy.CONTACT_SUPPORT);
        return new ResponseEntity<>(oo, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    private static RetryStrategy setRetryCoding(OperationOutcomeIssueComponent issue, RetryStrategy retry) {
        if (retry == null) {
            return null;
        }
        String[] parts = retry.toString().split(": ");
        Coding retryCode = new Coding(SoapMessage.HUB_NS + ":Fault:Retry", parts[0], parts[1]);
        issue.getDetails().addCoding(retryCode);
        return retry;
    }
}
