package gov.cdc.izgateway.soap.net;

import gov.cdc.izgateway.logging.event.EventId;
import gov.cdc.izgateway.soap.message.*;
import gov.cdc.izgateway.soap.message.SoapMessage.Response;
import gov.cdc.izgateway.utils.HL7Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.MDC;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.List;
import java.util.function.Function;

// TODO: Refactor this with IZG Core

public class SoapMessageWriter {
	public static final String HIDDEN = "[Hidden]";
	private static boolean fixNewlines = true;
	private final SoapMessage m;
	private final XMLStreamWriter w;
	private final boolean filtering;
	
	public SoapMessageWriter(SoapMessage m, XMLStreamWriter w, boolean filtering) {
		this.m = m;
		this.w = w;
		this.filtering = filtering;
	}
	
	public SoapMessageWriter(SoapMessage m, XMLStreamWriter w) {
		this(m, w, false);
	}
	
	public static void setFixNewLines(boolean fixNewlines) {
		SoapMessageWriter.fixNewlines = fixNewlines;
	}
	
	public static boolean getFixNewLines() {
		return SoapMessageWriter.fixNewlines;
	}
	
	public boolean isFiltering() {
		return filtering;
	}

	/**
	 * Adjust IIS schema based on message schema 
	 * @param m
	 * @return The correct schema to use for iis: prefix.
	 */
	public static String getIisSchema(SoapMessage m) {
		if (m.getSchema().contains("2011")) {
			return SoapMessage.IIS2011_NS;
		}
		return SoapMessage.IIS2014_NS;
	}

	public void writeOptionalTextElement(String name, String value) throws XMLStreamException {
		if (!StringUtils.isEmpty(value)) {
			w.writeStartElement(SoapMessage.IIS_PREFIX, m.elementName(name), getIisSchema(m));
				w.writeCharacters(value);
			w.writeEndElement();
		}
	}
	public void writeRequiredTextElement(String name, String value) throws XMLStreamException {
		w.writeStartElement(SoapMessage.IIS_PREFIX, m.elementName(name), getIisSchema(m));
		if (!StringUtils.isEmpty(value)) {
			w.writeCharacters(value);
		}
		w.writeEndElement();
	}
	
	private void writeSoapValueElement(String value) throws XMLStreamException {
		w.writeStartElement(SoapMessage.SOAP_PREFIX, "Value", SoapMessage.SOAP_NS);
		if (!StringUtils.isEmpty(value)) {
			w.writeCharacters(value);
		}
		w.writeEndElement();
	}
	private void writeSoapTextElement(String text) throws XMLStreamException {
		w.writeStartElement(SoapMessage.SOAP_PREFIX, "Text", SoapMessage.SOAP_NS);
		w.writeAttribute("xml", "http://www.w3.org/XML/1998/namespace", "lang", "en");
		if (!StringUtils.isEmpty(text)) {
			w.writeCharacters(text);
		}
		w.writeEndElement();
	}

	public void write() throws XMLStreamException {
		w.writeStartElement(SoapMessage.SOAP_PREFIX, "Envelope", SoapMessage.SOAP_NS);
		w.writeNamespace(SoapMessage.SOAP_PREFIX, SoapMessage.SOAP_NS);
		w.writeNamespace(SoapMessage.IIS_PREFIX, getIisSchema(m));
		
		if (!m.getWsaHeaders().isEmpty() || !m.getHubHeader().isEmpty() || !m.getXformHeader().isEmpty()) {
			w.writeStartElement(SoapMessage.SOAP_PREFIX, "Header", SoapMessage.SOAP_NS);
			w.writeNamespace(WsaHeaders.WSA_PREFIX, WsaHeaders.WSA_NS);
				writeWsaHeaders();
				writeHubHeaders(m instanceof Response);
            writeXformHeaders(m instanceof Response);
			w.writeEndElement();
		}

			w.writeStartElement(SoapMessage.SOAP_PREFIX, "Body", SoapMessage.SOAP_NS);
				startIisElement();
					if (m instanceof FaultMessage) {
						writeFaultContent();
					} else {
						writeBodyContent();
					}
				w.writeEndElement();
			
			w.writeEndElement();

		w.writeEndElement();
		w.flush();
	}
    private void writeHubHeaders(boolean isResponse) throws XMLStreamException {
        if (m.getHubHeader().isEmpty()) {
            return;
        }
        w.writeStartElement(
                SoapMessage.HUB_PREFIX,
                isResponse ? "HubResponseHeader" : "HubRequestHeader",
                SoapMessage.HUB_NS
        );
        // Force write of namespace prefix for hub header in case it isn't otherwise written.
        w.writeNamespace(SoapMessage.HUB_PREFIX, SoapMessage.HUB_NS);
        writeKeyValueSuppliers(HubHeader.getKeyValueSuppliers(), SoapMessage.HUB_PREFIX, SoapMessage.HUB_NS);
        w.writeEndElement();
    }

	private void writeXformHeaders(boolean isResponse) throws XMLStreamException {
		if (!isResponse || m.getXformHeader().isEmpty()) {
			return;
		}

		w.writeStartElement(
			SoapMessage.XFORM_PREFIX,
			"XformResponseHeader",
			SoapMessage.XFORM_NS
		);
		// Force write of namespace prefix for hub header in case it isn't otherwise written.
		w.writeNamespace(SoapMessage.XFORM_PREFIX, SoapMessage.XFORM_NS);

		// Xform headers always have HL7 content that needs to be protected if isFiltering is true
		writeKeyValueSuppliers(XformHeader.getKeyValueSuppliers(), SoapMessage.XFORM_PREFIX, SoapMessage.XFORM_NS, true);
		w.writeEndElement();
	}
	
	public void writeWsaHeaders() throws XMLStreamException {
		if (m.getWsaHeaders().isEmpty()) {
			return;
		}
		writeKeyValueSuppliers(WsaHeaders.getKeyValueSuppliers(), WsaHeaders.WSA_PREFIX, WsaHeaders.WSA_NS);
	}
	
	private void writeKeyValueSuppliers(List<Pair<String, Function<SoapMessage, String>>> pairs, String prefix, String ns) throws XMLStreamException {
        writeKeyValueSuppliers(pairs, prefix, ns, false);
	}

    private void writeKeyValueSuppliers(List<Pair<String, Function<SoapMessage, String>>> pairs, String prefix, String ns, boolean hasHl7Content) throws XMLStreamException {
        for (Pair<String, Function<SoapMessage, String>> pair: pairs) {
            String value = pair.getValue().apply(m);
            if (!StringUtils.isEmpty(value)) {
                w.writeStartElement(prefix, pair.getKey(), ns);
                if ( hasHl7Content && isFiltering() ) {
                    w.writeCharacters(HL7Utils.protectHL7Message(value));
                } else {
                    w.writeCharacters(value);
                }
                w.writeEndElement();
            }
        }
    }

	public void startIisElement(String name) throws XMLStreamException {
		w.writeStartElement(SoapMessage.IIS_PREFIX, m.elementName(name), getIisSchema(m));
	}
	
	private void startIisElement() throws XMLStreamException {
		if (m instanceof FaultMessage) {
			w.writeStartElement(SoapMessage.SOAP_PREFIX, "Fault", SoapMessage.SOAP_NS);
		} else {
			startIisElement(getMessageName());
		}
	}
	
	private String getMessageName() {
		Class<?> clazz = m.getClass();
		while (clazz != Object.class && clazz.getPackage() != SoapMessage.class.getPackage()) {
			clazz = clazz.getSuperclass();
		}
		return clazz.getSimpleName();
	}
	
	public void writeFaultContent() throws XMLStreamException {
		FaultMessage f = (FaultMessage) m;
		w.writeStartElement(SoapMessage.SOAP_PREFIX, "Code", SoapMessage.SOAP_NS);
			writeSoapValueElement("soap:Receiver");
		w.writeEndElement();
		w.writeStartElement(SoapMessage.SOAP_PREFIX, "Reason", SoapMessage.SOAP_NS);
			writeSoapTextElement(f.getReason());
		w.writeEndElement();
		w.writeStartElement(SoapMessage.SOAP_PREFIX, "Detail", SoapMessage.SOAP_NS);
		w.writeNamespace("", "");
			// depending on the fault, we may need to add the Hub prefix and namespace
			startFaultName(f);
			if (f.isHubFault()) {
				w.writeStartElement(SoapMessage.HUB_PREFIX, "DestinationId", SoapMessage.HUB_NS);
				{	String destId = f.getDestinationId();
					if (!StringUtils.isEmpty(destId)) {
						w.writeCharacters(destId);
					}
				}
				w.writeEndElement();
				if (!("UnknownDestinationFault".equals(f.getFaultName()))) {
					w.writeStartElement(SoapMessage.HUB_PREFIX, "DestinationUri", SoapMessage.HUB_NS);
					{	String destUri = f.getDestinationUri();
						if (!StringUtils.isEmpty(destUri)) {
							w.writeCharacters(destUri);
						}
					}
					w.writeEndElement();
				}
			} else if ("MessageTooLargeFault".equals(f.getFaultName())) {
				w.writeStartElement(SoapMessage.IIS_PREFIX, "Size", getIisSchema(m));
					w.writeCharacters(f.getSize());
				w.writeEndElement();
				w.writeStartElement(SoapMessage.IIS_PREFIX, "MaxSize", getIisSchema(m));
				w.writeCharacters(f.getMaxSize());
				w.writeEndElement();
			}
			w.writeEndElement();
			if (SoapMessage.IIS2011_NS.equals(m.getSchema())) {
				writeOptionalTextElement("Code", f.getCode());
				writeOptionalTextElement("Reason", f.getReason());
				writeOptionalTextElement("Detail", f.getDetail());
			}
			writeNoNamespaceElement("EventID", MDC.get(EventId.EVENTID_KEY));
			writeNoNamespaceElement("Summary", StringUtils.join(f.getSummary().split("\\s+")));
			writeNoNamespaceElement("Detail", f.getDetail());
			writeNoNamespaceElement("Diagnostics", f.getDiagnostics());
			writeNoNamespaceElement("Retry", f.getRetry());
			if ("HubClientFault".equals(f.getFaultName())) {
				writeNoNamespaceElement("Original", f.getOriginal());
			}
		w.writeEndElement();
	}
	
	private void writeNoNamespaceElement(String name, String value) throws XMLStreamException {
		if (!StringUtils.isEmpty(value)) {
			w.writeStartElement("", name, "");
				w.writeCharacters(value);
			w.writeEndElement();
		}
	}
	private void startFaultName(FaultMessage f) throws XMLStreamException {
		if (f.isHubFault()) {
			w.writeStartElement(SoapMessage.HUB_PREFIX, f.getFaultName(), SoapMessage.HUB_NS);
			w.writeNamespace(SoapMessage.HUB_PREFIX, SoapMessage.HUB_NS);
		} else {
			w.writeStartElement(SoapMessage.IIS_PREFIX, f.getFaultName(), getIisSchema(m));
			w.writeNamespace(SoapMessage.IIS_PREFIX, getIisSchema(m));
		}
	}
	public void writeBodyContent() throws XMLStreamException {
		if (m instanceof HasCredentials credentialed) {
			writeOptionalTextElement("Username", hidden(credentialed.getUsername()));
			writeOptionalTextElement("Password", hidden(credentialed.getPassword()));
			if (m instanceof HasFacilityID hfid) {
				writeOptionalTextElement("FacilityID", hfid.getFacilityID());
			}
		}
		if (m instanceof HasHL7Message hl7m) {
			writeHl7Message(hl7m.getHl7Message(), hl7m.isCdataWrapped());
		}
		if (m instanceof HasEchoBack heb) {
			writeRequiredTextElement("EchoBack", heb.getEchoBack());
		}
	}
	
	private String hidden(String value) {
		if (StringUtils.isEmpty(value)) {
			return value;
		}
		return filtering ? HIDDEN : value;
	}

	/**
	 * Write out an HL7 message properly escaped for representation in XML.
	 * @param w
	 * @param hl7Message
	 * @param b 
	 * @throws XMLStreamException
	 */
	private void writeHl7Message(String hl7Message, boolean isCdataWrapped) throws XMLStreamException {
		w.writeStartElement(SoapMessage.IIS_PREFIX, m.elementName("Hl7Message"), m.getSchema());
		if (filtering) {
			hl7Message = HL7Utils.protectHL7Message(hl7Message);
		}
		
		if (!StringUtils.isEmpty(hl7Message)) {
			if (isCdataWrapped) {
				w.writeCData(hl7Message);
			} else if (fixNewlines) {
				String[] data = hl7Message.split("\r");
				w.writeCharacters(data[0]);
				for (int i = 1; i < data.length; i++) {
					w.writeEntityRef("#xD");
					w.writeCharacters(data[i]);
				}
			} else {
				w.writeCharacters(hl7Message);
			}
		}
		w.writeEndElement();
	}
}
