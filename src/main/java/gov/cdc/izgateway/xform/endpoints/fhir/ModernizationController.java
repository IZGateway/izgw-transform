package gov.cdc.izgateway.xform.endpoints.fhir;

import gov.cdc.izgateway.security.AccessControlRegistry;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ServiceConfigurationError;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.XMLUtils;
import gov.cdc.izgateway.security.Roles;
import gov.cdc.izgw.v2tofhir.converter.MessageParser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * The Modernization Controller implements methods enabling users to convert data between HL7 Formats,
 * e.g., HL7 Version 2, HL7 CDA and C-CDA, and HL7 FHIR.
 * 
 * The base capability of this component is to convert a V2 message into FHIR Resources
 * 
 * 
 * @author Audacious Inquiry 
 */
@RestController
@RolesAllowed({Roles.SOAP, Roles.ADMIN, Roles.USERS})
@RequestMapping("/fhir")
@Slf4j
@Lazy(false)
public class ModernizationController {
	Transformer transformer = loadCdaTransformer();
	
	private static Transformer loadCdaTransformer() {
		TransformerFactory factory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);
		try {
			return factory.newTransformer(new StreamSource(ModernizationController.class.getClassLoader().getResourceAsStream("fhir2cda.xslt")));
		} catch (TransformerConfigurationException e) {
			throw new ServiceConfigurationError("Cannot load fhir2cda.xslt", e);
		}
	}
	/**
	 * Conversion input
	 * @author Audacious Inquiry
	 */
	@Data
	public static class Input {
		/** The input content as a string */
		String body;
		/** The input content as a resource */
		Resource resource;
		/**
		 * Construct a new empty input object
		 */
		public Input() {
		}
		/**
		 * Construct an input object with a body
		 * @param body The body
		 */
		public Input(String body) {
			this.body = body;
		}
	}
	/**
	 * Construct the FhirController.  It calls HubController methods directly so
	 * needs to know where the hub is.
	 * @param registry The access control registry
	 */
	public ModernizationController(AccessControlRegistry registry) {
		registry.register(this);
	}
	
    /**
     * Perform the convert operation. 
     * 
     * @param body	The operation parameters, as a Parameters resource, a Binary Resource containing a V2 message or CDA document, 
     * a V2 message, a CDA Document, a FHIR Resource or Bundle.
     * @param req	The HttpServletRequest so we can process parameters.
     * @return	The converted content as a FHIR Bundle or Resource
     */
    @Operation(
    	summary = "Perform the $convert operation",
        description = "Convert the requested content to FHIR"
    )
    @ApiResponse(
        responseCode = "200",
        description = "The request completed normally",
        content = {@Content(
                mediaType = "application/xml"
        )}
    )
    @ApiResponse(
    	responseCode = "400",
        description = "The request was invalid",
        content = {@Content}
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
    @RequestMapping(value= "/$convert", 
    	method = { 
    		RequestMethod.POST,	// Typical web based query 
    		RequestMethod.HEAD	// Used with SMART and other auth mechanisms.
    	},
        produces = {
        	ContentUtils.FHIR_PLUS_XML_VALUE, 	// FHIR Content could be binary, params w/ Binary, or FHIR
        	ContentUtils.FHIR_PLUS_JSON_VALUE, 	
        	ContentUtils.FHIR_PLUS_YAML_VALUE,		
        	
        	MediaType.APPLICATION_JSON_VALUE,	// FHIR
        	ContentUtils.YAML_VALUE,			// FHIR
        	
        	MediaType.APPLICATION_XML_VALUE,	// Could be CDA+XML, FHIR+XML
        	"text/xml",							// Could be CDA+XML, FHIR+XML
        	
        	"application/hl7-sda+xml",	// HL7 CDA
        	ContentUtils.CDA_VALUE,		// HL7 CDA
        	
    	}, consumes = {
    		ContentUtils.FHIR_PLUS_XML_VALUE, 	// FHIR Content could be binary, params w/ Binary, or FHIR
    		ContentUtils.FHIR_PLUS_JSON_VALUE, 	
    		ContentUtils.FHIR_PLUS_YAML_VALUE,		
        	
        	MediaType.APPLICATION_JSON_VALUE,	// FHIR
        	ContentUtils.YAML_VALUE,	// FHIR
        	
        	MediaType.APPLICATION_XML_VALUE,	// Could be CDA+XML, FHIR+XML, V2+XML
        	"text/xml",					// Could be CDA+XML, FHIR+XML, V2+XML
        	
        	ContentUtils.CDA_VALUE,		// HL7 CDA
        	"application/hl7-sda+xml",	// HL7 CDA
        	
        	ContentUtils.HL7V2_XML_VALUE,	// V2 XML
        	"application/x.hl7v2+xml",	// V2 XML
        	"application/hl7-v2+xml",	// V2 XML
        	
        	"application/hl7v2+er7",	// V2 ER7
        	"application/x.hl7v2+er7",	// V2 ER7
        	"application/hl7-v2+er7",	// V2 ER7

        	
        	ContentUtils.HL7V2_TEXT_VALUE,
        	MediaType.TEXT_PLAIN_VALUE
		}
    )
    
    public ResponseEntity<Resource> convert(
    	@RequestBody String body,
    	HttpServletRequest req
    ) {
    	Input input = new Input(body);
    	
    	String contentType = normalizeContentType(req.getHeader(HttpHeaders.CONTENT_TYPE), input); 
    	if (contentType == null) {
    		throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unrecognized Content Type");
    	}
    	Resource r = null;
    	switch (contentType) {
    	case ContentUtils.HL7V2_TEXT_VALUE:
    		r = convertToFhirFromV2(body);
    		break;
    	case ContentUtils.CDA_VALUE:
    		r = convertToFhirFromCDA(body);
    		break;
    		
    	case ContentUtils.FHIR_PLUS_XML_VALUE, 	
    		ContentUtils.FHIR_PLUS_JSON_VALUE, 	
    		ContentUtils.FHIR_PLUS_YAML_VALUE:
        	r = input.getResource();
    		break;
    	default:
    		break;
    	}
    	
    	HttpHeaders headers = ContentUtils.getHeaders2(req);
    	switch (headers.getContentType().toString()) {
    	case ContentUtils.FHIR_PLUS_XML_VALUE,	
    		ContentUtils.FHIR_PLUS_JSON_VALUE, 	
    		ContentUtils.FHIR_PLUS_YAML_VALUE:
    		break;
    	case ContentUtils.CDA_VALUE:
    		r = convertToCDA(r);
    		break;
    	case ContentUtils.HL7V2_TEXT_VALUE:
    		r = convertToV2Text(r);
    		break;
    	case ContentUtils.HL7V2_XML_VALUE:
    		r = convertToV2XML(r);
    		break;
    	default:
    		break;
    	}
    	return new ResponseEntity<>(r, headers, HttpStatus.OK);
    }
    
	private Resource convertToFhirFromV2(String body) {
    	MessageParser mp = new MessageParser();
    	try {
			return mp.convert(body);
		} catch (HL7Exception e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	private Resource convertToFhirFromCDA(String body) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private Binary convertToCDA(Resource r) {
		String content = ContentUtils.FHIR_XML_PARSER.encodeResourceToString(r);
		try (StringWriter sw = new StringWriter()){
			transformer.transform(new StreamSource(new StringReader(content)), new StreamResult(sw));
			Binary b = new Binary();
			b.setContentType(ContentUtils.CDA_VALUE);
			b.setData(sw.toString().getBytes(StandardCharsets.UTF_8));
			return b;
		} catch (TransformerException e) {
			
		} catch (IOException e1) {
			// Ignore IO Exceptions on close, we are working with Strings, there should be none
		}
		return null;
	}
	private Binary convertToV2Text(Resource r) {
		if (!(r instanceof Bundle b)) {
			return null;
		}
		// Find the first DocumentReference resource in the Bundle
		DocumentReference ref = b.getEntry().stream()
			.map(e -> e.getResource())
			.filter(DocumentReference.class::isInstance)
			.map(DocumentReference.class::cast)
			.findFirst().orElse(null);
		if (ref != null) {
			Attachment att = ref.getContentFirstRep().getAttachment();
			Binary binary = new Binary();
			binary.setContent(att.getData());
			binary.setContentType(att.getContentType());
			return binary;
		}
		return null;
	}
	private Binary convertToV2XML(Resource r) {
		Binary b = convertToV2Text(r);
		if (b == null) {
			return null;
		}
		String msg = new String(b.getData(), StandardCharsets.UTF_8);
		try {
			Message m = new PipeParser().parse(msg);
		    DefaultXMLParser xmlParser = new DefaultXMLParser(m.getParser().getFactory());
		    String xmlString = xmlParser.encode(m);
		    b.setData(xmlString.getBytes(StandardCharsets.UTF_8));
		    b.setContentType(ContentUtils.HL7V2_XML_VALUE);
		    return b;
		} catch (HL7Exception e) {
			return null;
		}
	}
	private static String normalizeContentType(String contentType, Input input) {
		switch (contentType) {
		case ContentUtils.FHIR_PLUS_JSON_VALUE, 	// FHIR Content could be binary, params w/ Binary, or FHIR
			ContentUtils.FHIR_PLUS_YAML_VALUE,		// FHIR
			MediaType.APPLICATION_JSON_VALUE, 		// FHIR
			ContentUtils.YAML_VALUE:				// FHIR
			
			return detectContentTypeFromResource(contentType, input);
		
		case MediaType.TEXT_XML_VALUE, 
			MediaType.APPLICATION_XML_VALUE:		// Could be CDA+XML, FHIR+XML, V2+XML
			return detectContentTypeFromXML(contentType, input);
			
		case ContentUtils.HL7V2_XML_VALUE,	// V2 XML
			"application/x.hl7v2+xml",	// V2 XML
			"application/hl7-v2+xml":	// V2 XML
			return ContentUtils.HL7V2_XML_VALUE;
		
		case ContentUtils.HL7V2_TEXT_VALUE,
			"application/hl7v2+er7",	// V2 ER7
			"application/x.hl7v2+er7",	// V2 ER7
			"application/hl7-v2+er7":	// V2 ER7
			
			return ContentUtils.HL7V2_TEXT_VALUE;
		
		case "application/hl7-sda+xml",	// HL7 CDA
			ContentUtils.CDA_VALUE:		// HL7 CDA
			return ContentUtils.CDA_VALUE;

		default:
			return detectContentTypeFromBody(input);
		}
	}
	
	private static String detectContentTypeFromResource(String contentType, Input input) {
		if (input.body == null || input.body.isBlank()) {
			return null;
		}
		IParser p = null;
		if (contentType.contains("xml")) {
			p = ContentUtils.FHIR_XML_PARSER;
			contentType = ContentUtils.FHIR_PLUS_XML_VALUE;
		} else if (contentType.contains("yaml")) {
			p = ContentUtils.FHIR_YAML_PARSER;
			contentType = ContentUtils.FHIR_PLUS_YAML_VALUE;
		} else {
			p = ContentUtils.FHIR_JSON_PARSER;
			contentType = ContentUtils.FHIR_PLUS_JSON_VALUE;
		}
		Resource r = (Resource) p.parseResource(input.body);
		if (r instanceof Parameters params) {
			input.body = getParameter(params, "data");
			return getParameter(params, "contentType");
		} 
		
		if (r instanceof Binary b) {
			contentType = b.getContentType();
			if (contentType.contains("fhir")) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
			input.body = new String(b.getData(), StandardCharsets.UTF_8);
			return normalizeContentType(contentType, input);
		} 
		input.setResource(r);
		return contentType;
	}
	
	private static String getParameter(Parameters p, String name) {
		ParametersParameterComponent v = p.getParameter(name);
		if (v == null || v.isEmpty()) {
			return null;
		}
		return v.primitiveValue();
	}

	private static String detectContentTypeFromXML(String contentType, Input input) {
		try {
			Document doc = XMLUtils.parse(input.body);
			Element root = doc.getDocumentElement();
			switch (root.getNamespaceURI()) {
			case "urn:hl7-org:v3":		// CDA
				return ContentUtils.CDA_VALUE;
			case "http://hl7.org/fhir":	// FHIR
				// Force read of FHIR Resource here
				return detectContentTypeFromResource(contentType, input);
			case "urn:hl7-org:v2xml":	// HL7 V2
				return ContentUtils.HL7V2_XML_VALUE;
			default:
				return null;
			}
		} catch (Exception ex) {
			return null;
		}
	}
	
    private static String detectContentTypeFromBody(Input input) {
    	try {
			return ContentUtils.guessMediaType(IOUtils.toInputStream(input.body, StandardCharsets.UTF_8)).toString();
		} catch (IOException e) {
			return null;
		}
	}
}
