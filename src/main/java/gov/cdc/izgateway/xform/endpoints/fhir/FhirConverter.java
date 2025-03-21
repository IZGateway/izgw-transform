package gov.cdc.izgateway.xform.endpoints.fhir;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.ainq.fhir.utils.YamlParser;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

/**
 * This is a converter to and from FHIR for SpringBoot applications that are
 * not using the HAPI on FHIR native web server. 
 */
public class FhirConverter implements HttpMessageConverter<Resource> {
	private static final boolean PRETTY = true;
	/** Unicode Byte Order Mark is NOT considered to be whitespace */
	private static final char BOM = '\uFEFF';
	private static final FhirContext R4 = FhirContext.forR4();
	private final IParser fhirParser = R4.newJsonParser().setPrettyPrint(PRETTY);
	private final IParser xmlParser = R4.newXmlParser().setPrettyPrint(PRETTY);
	private final IParser yamlParser = new YamlParser(R4).setPrettyPrint(PRETTY);
	/** Mime Type for FHIR in JSON format */
	public static final String FHIR_PLUS_JSON_VALUE = "application/fhir+json";
	/** Media Type for FHIR in JSON format */
	public static final MediaType FHIR_PLUS_JSON = parseMediaType(FHIR_PLUS_JSON_VALUE);
	/** Mime Type for FHIR in XML format */
	public static final String FHIR_PLUS_XML_VALUE = "application/fhir+xml";
	/** Media Type for FHIR in XML format */
	public static final MediaType FHIR_PLUS_XML = parseMediaType(FHIR_PLUS_XML_VALUE);
	/** Mime Type for FHIR in YAML format */
	public static final String FHIR_PLUS_YAML_VALUE = "application/fhir+yaml";
	/** Media Type for FHIR in YAML format */
	public static final MediaType FHIR_PLUS_YAML = parseMediaType(FHIR_PLUS_YAML_VALUE);
	/** Mime Type for YAML format */
	public static final String YAML_VALUE = "application/yaml";
	/** Media Type for YAML format */
	public static final MediaType YAML = parseMediaType(YAML_VALUE);
	
	/** Supported FHIR Media Types */
	public static final List<MediaType> FHIR_MEDIA_TYPES = Arrays.asList(
		FHIR_PLUS_JSON,
		FHIR_PLUS_XML,
		FHIR_PLUS_YAML,
		YAML,
		MediaType.APPLICATION_JSON,
		MediaType.APPLICATION_XML,
		MediaType.TEXT_XML
	);
	
	private static MediaType parseMediaType(String contentType) {
		String[] parts = contentType.split("/");
		return new MediaType(parts[0], parts[1]);
	}
	
	@Override
	public boolean canRead(Class<?> clazz, MediaType mediaType) {
		mediaType = mediaType == null ? null : new MediaType(mediaType.getType(), mediaType.getSubtype());
		return Resource.class.isAssignableFrom(clazz) && (mediaType == null || FHIR_MEDIA_TYPES.contains(mediaType));
	}

	@Override
	public boolean canWrite(Class<?> clazz, MediaType mediaType) {
		return canRead(clazz, mediaType);
	}

	@Override
	public List<MediaType> getSupportedMediaTypes() {
		return FHIR_MEDIA_TYPES;
	}

	@Override
	public Resource read(Class<? extends Resource> clazz, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {
		
		IParser parser = null;
		String contentType = inputMessage.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
		MediaType mediaType = null;
		
		BufferedInputStream bis = IOUtils.buffer(inputMessage.getBody());
		if (contentType == null || contentType.contains("json")) {
			mediaType = MediaType.APPLICATION_JSON;
		} else if (contentType.contains("xml")) {
			mediaType = MediaType.APPLICATION_XML; 
		} else if (contentType.contains("yaml")) {
			mediaType = YAML;
		} else {
			mediaType = guessMediaType(bis);
		}
	
		parser = selectParser(mediaType);
		return (Resource) parser.parseResource(bis);
	}

	/**
	 * Given a media type, return the appropriate parser for reading and generating it
	 * @param mediaType	The media type
	 * @return	A parser for reading and generating FHIR from th specified media type
	 */
	public IParser selectParser(MediaType mediaType) {
		// Simplify the media type.
		mediaType = new MediaType(mediaType.getType(), mediaType.getSubtype());
		// Convert to string for switch
		String mimeType = StringUtils.defaultString(mediaType.toString());
		switch (mimeType) {
		case FHIR_PLUS_XML_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE:
			return xmlParser;
			
		case FHIR_PLUS_YAML_VALUE, YAML_VALUE:
			return yamlParser;
			
		case FHIR_PLUS_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE:
		default:
			return fhirParser;
		}
	}

	@Override
	public void write(Resource resource, MediaType contentType, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		IParser parser = selectParser(contentType);
		OutputStreamWriter w = null;
		try {  // NOSONAR: It's up to the caller to determine whether the body gets closed when this is done.
			w = new OutputStreamWriter(outputMessage.getBody());
			parser.encodeResourceToWriter(resource, w);
		} finally {
			if (w != null) {
				w.flush();
			}
		}
	}

	/**
	 * Guess the media type of an input stream and return it.
	 * @param bis	The input stream (must be a buffered input stream 
	 * to address the issue of read-ahead.
	 * @return	The best guess at the media type for this resource.
	 * @throws IOException	If an IO Error occurs.
	 */
	public MediaType guessMediaType(BufferedInputStream bis) throws IOException {
		bis.mark(512);
		try {
			for (int i = 0; i < 512; i++) {
				int c = bis.read();
				if (c < 0) {
					break;
				} else if (c == '<') {
					return FHIR_PLUS_XML;
				} else if (c == '{') {
					return FHIR_PLUS_JSON;
				} else if (c == '-') {
					return FHIR_PLUS_YAML;
				} else if (!Character.isWhitespace(c) && c != BOM) {
					// It's a good guess.
					return FHIR_PLUS_YAML;
				}
			}
		} finally {
			bis.reset();
		}
		return FHIR_PLUS_JSON;
	}
}
