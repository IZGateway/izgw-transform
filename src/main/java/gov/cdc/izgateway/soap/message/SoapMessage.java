package gov.cdc.izgateway.soap.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Refactor this with IZG Core

/**
 * Base class for SOAP messages that IZ Gateway sends and receives.
 */
@Schema(description="A generic SOAP Request message for IZ Gateway")
@Data
@NoArgsConstructor
public class SoapMessage implements Serializable {
	private static final long serialVersionUID = 1L;
	/** The SOAP 1.2 envelope namespace URI. */
	public static final String SOAP_NS = "http://www.w3.org/2003/05/soap-envelope";
	/** The namespace prefix for SOAP envelope elements. */
	public static final String SOAP_PREFIX = "soap";
	/** The IZ Gateway Hub namespace URI. */
	public static final String HUB_NS = "urn:cdc:iisb:hub:2014";
	/** The namespace prefix for Hub elements. */
	public static final String HUB_PREFIX = "hub";
    /** The transformation service namespace URI. */
    public static final String XFORM_NS = "urn:cdc:iisb:hub:xform";
    /** The namespace prefix for transformation service elements. */
    public static final String XFORM_PREFIX = "xform";
	/** The CDC 2011 IIS WSDL namespace URI. */
	public static final String IIS2011_NS = "urn:cdc:iisb:2011";
	/** The namespace prefix for IIS elements. */
	public static final String IIS_PREFIX = "iis";
	/** The CDC 2014 IIS WSDL namespace URI. */
	public static final String IIS2014_NS = "urn:cdc:iisb:2014";
	/** A map of schema names. Not used in this repository. */
	protected static final Map<String, String> schemaNameMap = new HashMap<>();
	/** The media types accepted for SOAP messages. */
	protected static final String[] SOAP_MEDIA_TYPES = {
			MediaType.TEXT_PLAIN_VALUE,
			MediaType.TEXT_HTML_VALUE,	// Postel's law, be forgiving
			MediaType.TEXT_XML_VALUE,
			MediaType.APPLICATION_XML_VALUE,
			"application/soap",
			"application/soap+xml"
	};
	/** The media types accepted for WSDL documents. */
	protected static final String[] WSDL_MEDIA_TYPES = {
			MediaType.TEXT_PLAIN_VALUE,
			MediaType.TEXT_HTML_VALUE,	// Postel's law, be forgiving
			MediaType.TEXT_XML_VALUE,
			MediaType.APPLICATION_XML_VALUE,
			"application/soap+xml",
			"application/soap",
			"application/wsdl+xml"
		};
	
	/** Marker interface for request messages */
	public static interface Request {}
	/** Marker interface for response messages */
	public static interface Response {}
	/** The XML schema namespace of this message. */
	@Schema(description="The XML Schema this message comes from")
	private String schema = IIS2014_NS;

	/** The Hub header values from the soap:Header element. */
	@Schema(description="The Hub Request header values found in the soap:Header element if any")
	private HubHeader hubHeader = new HubHeader();
	/** The WS-Addressing header values from the soap:Header element. */
	@Schema(description="The Web Services Addressing (WSA) values found in the soap:Header element if any")
	private WsaHeaders wsaHeaders = new WsaHeaders();
    /** The transformation service header values from the soap:Header element. */
    @Schema(description="The Xform Request header values found in the soap:Header element if any")
    private XformHeader xformHeader = new XformHeader();
	/**
	 * This constructor serves several functions.
	 * 1. It supports upgrade during marshalling of inbound content from SoapMessage to a more specific type.
	 * 2. It supports creation of a copy of an inbound messages (from a requstor) to an outbound message (to a destination) using a different schema. 
	 * 3. It supports initial creation of a response class from a request class for performing work.  The last phase after using the constructor
	 * for this purpose should be to call relatesTo() to indicate that the newly created message is a reply to the original message.
	 * 
	 * @param that	The message to copy from.
	 * @param schema	The schema to use.
	 * @param isUpgradeOrSchemaChange true if this is an upgrade or schema change; false to make this message a response to {@code that}
	 */
	public SoapMessage(SoapMessage that, String schema, boolean isUpgradeOrSchemaChange) {
		// Always copy the HubHeader
		this.hubHeader.copyFrom(that.hubHeader);
		if (isUpgradeOrSchemaChange) {
			// It's an upgrade or schema change
			this.wsaHeaders.copyFrom(that.wsaHeaders);
		} else {
			this.respondingTo(that);
		}
		setSchema(schema == null ? that.schema : schema);
	}

	/**
	 * Set the XML schema namespace of this message.
	 * @param schema The schema namespace URI.
	 */
	public void setSchema(String schema) {
		this.schema = schema;
	}

	/**
	 * Set the WS-Addressing Action header to the action for this message.
	 * @param isHub true to use the Hub WSDL action, false to use the IIS WSDL action.
	 * @return This message.
	 */
	public SoapMessage updateAction(boolean isHub) {
		String action = getAction(isHub);
		this.getWsaHeaders().setAction(action);
		return this;
	}
	
	/**
	 * Get the WS-Addressing action URI for this message, based on its type and schema.
	 * @param isHub true to use the Hub WSDL action, false to use the IIS WSDL action.
	 * @return The action URI.
	 */
	public String getAction(boolean isHub) {
		String simpleName = this.getClass().getSimpleName();
				
		if (isHub) {
			if (this instanceof FaultMessage fm) {
			    switch (fm.getFaultName()) {
			    case "UnsupportedOperationFault": return "urn:cdc:iisb:hub:2014:IISHubPortType:ConnectivityTest:Fault:UnsupportedOperationFault";
			    case "DestinationConnectionFault": return "urn:cdc:iisb:hub:2014:IISHubPortType:SubmitSingleMessage:Fault:DestinationConnectionFault";
			    case "UnknownDestinationFault": return "urn:cdc:iisb:hub:2014:IISHubPortType:SubmitSingleMessage:Fault:UnknownDestinationFault";
			    case "HubClientFault": return "urn:cdc:iisb:hub:2014:IISHubPortType:SubmitSingleMessage:Fault:HubClientFault";
			    case "MessageTooLargeFault": return "urn:cdc:iisb:hub:2014:IISHubPortType:SubmitSingleMessage:Fault:MessageTooLargeFault";
			    case "SecurityFault": return "urn:cdc:iisb:hub:2014:IISHubPortType:SubmitSingleMessage:Fault:SecurityFault";
			    default: return "urn:cdc:iisb:hub:2014:IISHubPortType:SubmitSingleMessage:Fault";
			    }
			}
	    	return "urn:cdc:iisb:hub:2014:IISHubPortType:" + simpleName;
		}
		
		if (is2011Message()) {
			if (this instanceof FaultMessage fm) {
				return "urn:cdc:iisb:2011:Fault:" + to2011name(fm.getFaultName());
			}
		    switch (this.getClass().getSimpleName()) {
		    case "ConnectivityTestRequest": return "urn:cdc:iisb:2011:connectivityTest";
		    case "ConnectivityTestResponse": return "urn:cdc:iisb:2011:connectivityTestResponse";
		    case "SubmitSingleMessageRequest": return "urn:cdc:iisb:2011:submitSingleMessage";
		    case "SubmitSingleMessageResponse": return "urn:cdc:iisb:2011:submitSingleMessageResponse";
		    default: return "urn:cdc:iisb:2011:" + this.to2011name(simpleName);
		    }
		}


		if (this instanceof FaultMessage fm) {
		    switch (fm.getFaultName()) {
		    case "UnsupportedOperationFault": return "urn:cdc:iisb:2014:IISPortType:ConnectivityTest:Fault:UnsupportedOperationFault";
		    case "DestinationConnectionFault": return "urn:cdc:iisb:2014:IISPortType:SubmitSingleMessage:Fault:DestinationConnectionFault";
		    case "UnknownDestinationFault": return "urn:cdc:iisb:2014:IISPortType:SubmitSingleMessage:Fault:UnknownDestinationFault";
		    case "HubClientFault": return "urn:cdc:iisb:2014:IISPortType:SubmitSingleMessage:Fault:HubClientFault";
		    case "MessageTooLargeFault": return "urn:cdc:iisb:2014:IISPortType:SubmitSingleMessage:Fault:MessageTooLargeFault";
		    case "SecurityFault": return "urn:cdc:iisb:2014:IISPortType:SubmitSingleMessage:Fault:SecurityFault";
		    default: return "urn:cdc:iisb:2014:IISPortType:SubmitSingleMessage:Fault" + fm.getFaultName();
		    }
	    }
		    
		return "urn:cdc:iisb:2014:IISPortType:" + simpleName;
	}
	
	/**
	 * Mark this message as a response to a request, if this is a response and that is a request.
	 * @param that The request message.
	 * @return This message.
	 */
	public SoapMessage respondingTo(SoapMessage that) {
		if (this instanceof Response && that instanceof Request) {
			wsaHeaders.respondingTo(that.getWsaHeaders());
		}
		return this;
	}
	
	private static List<String> responseNames = Arrays.asList("Hl7Message", "EchoBack");
	private static Map<String, String> nameMap = new HashMap<>();
	
	/**
	 * Get the name of the element based on the name, the schema and the message type.
	 * @param name The 2014 WSDL name of the element
	 * @return The 2014 name if no adjustment is needed, or the original 2011 name if adjusted.
	 */
	public String elementName(String name) {
		if (!is2011Message()) {
			return name;
		}
		return to2011name(name);
	}
	
	/**
	 * Convert a 2014 SOAP Message Type Name to 2011 WSDL
	 * @param name The name of the request type	
	 * @return The 2011 name for the request
	 */
	private String to2011name(String name) {
		String newName = nameMap.get(name);
		if (newName != null) {
			return newName;
		}
		StringBuilder b = new StringBuilder(name);
		if (this instanceof Response && responseNames.contains(name)) {
			return "return";
		}
		// Lowercase the initial character
		b.setCharAt(0, Character.toLowerCase(b.charAt(0)));
		// Remove the trailing Request on the name 
		if (name.endsWith("Request")) {
			b.setLength(b.length() - 7);
		}
		newName = b.toString();
		nameMap.put(name, newName);
		return newName;
	}
	
	/**
	 * Check whether the Hub header has a destination identifier.
	 * @return true if the destination identifier is present and not empty.
	 */
	public boolean hasDestinationId() {
		return hubHeader != null && !StringUtils.isEmpty(hubHeader.getDestinationId());
	}

	/**
	 * Check whether this message has a schema.
	 * @return true if the schema is not null.
	 */
	public boolean hasSchema() {
		return schema != null;
	}

	/**
	 * Check whether this message uses the 2011 IIS WSDL schema.
	 * @return true if the schema is the 2011 IIS namespace.
	 */
	@JsonIgnore
	public boolean is2011Message() {
		return IIS2011_NS.equals(schema);
	}
	/**
	 * Check whether this message uses the 2014 IIS WSDL schema.
	 * @return true if this is a 2014 IIS message, false if it is a 2011 or Hub message.
	 */
	@JsonIgnore
	public boolean is2014Message() {
		if (IIS2011_NS.equals(schema)) {
			return false;
		}
		if (this instanceof FaultMessage fm && fm.isHubFault()) {
			return false;
		}
		return hubHeader.isEmpty() || IIS2014_NS.equals(schema);
	}
	/**
	 * Check whether this message uses the Hub WSDL schema.
	 * @return true if this is neither a 2011 nor a 2014 IIS message.
	 */
	@JsonIgnore
	public boolean isHubMessage() {
		return !is2011Message() && !is2014Message();
	}
	
	/**
	 * Extract the text case identifier from the message.
	 * @return The test case identifier or the empty string if there is none.
	 */
	public String findTestCaseIdentifier() {
		if (!(this instanceof HasHL7Message)) {
			return "";
		}
		
		String message = ((HasHL7Message)this).getHl7Message();
		
        if (StringUtils.isEmpty(message)) {
        	return "";
        }
        
        String[] fields = message.split("\\|", 10);
        String msh5 = fields.length > 4 ? fields[4] : "";
        String msh6 = fields.length > 5 ? fields[5] : "";

        if ("TEST".equalsIgnoreCase(msh5) && msh6 != null && msh6.matches("^TC_[0-9A-Z_]+$|^MOCK.*|^PERF.*$")) {
            return msh6;
        }
        
        return "";
	}

	/**
	 * Return the length of the message payload.
	 * @return Return the length of the payload.
	 */
	@JsonIgnore
	public int length() {
		if (this instanceof HasEchoBack echo) {
			return echo.getEchoBack().length();
		} 
		if (this instanceof HasHL7Message hl7) {
			return hl7.getHl7Message().length();
		}
		return 0;
	}
}
