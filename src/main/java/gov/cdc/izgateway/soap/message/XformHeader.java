package gov.cdc.izgateway.soap.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import gov.cdc.izgateway.common.HasDestinationUri;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

// TODO: Refactor this with IZG Core

/**
 * The transformation service header in a SOAP message.
 */
@Data 
@Schema(description="The Hub Request header in the SOAP message")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class XformHeader implements Serializable {
	private static final long serialVersionUID = 1L;

    /** The request after transformation. */
    @Schema(description="The transformed request.")
    private String transformedRequest;
    /** The response before transformation. */
    @Schema(description="The original response prior to xform.")
    private String originalResponse;

	private static List<Pair<String, Function<SoapMessage, String>>> pairs = Arrays.asList(
            Pair.of("TransformedRequest", (Function<SoapMessage, String>)(m -> m.getXformHeader().getTransformedRequest())),
            Pair.of("OriginalResponse", (Function<SoapMessage, String>)(m -> m.getXformHeader().getOriginalResponse()))
		);
		
	/**
	 * Create an empty header.
	 */
	public XformHeader() {
		// All fields start as null.
	}

	/**
	 * Get the element names and value getters for the header elements.
	 * @return The pairs of element name and function that gets the value from a message.
	 */
	@JsonIgnore
	public static List<Pair<String, Function<SoapMessage, String>>> getKeyValueSuppliers() {
		return pairs;
	}

	/**
	 * Check whether the header has no values.
	 * @return true if all values are empty.
	 */
	@JsonIgnore
	public boolean isEmpty() {
		return StringUtils.isAllEmpty(transformedRequest, originalResponse);
	}
	
	/**
	 * Remove all values from the header.
	 * @return This header.
	 */
	public XformHeader clear() {
		transformedRequest = originalResponse = null;
		return this;
	}

	/**
	 * Copy all values from another header.
	 * @param that The header to copy from.
	 * @return This header.
	 */
	public XformHeader copyFrom(XformHeader that) {
        this.transformedRequest = that.transformedRequest;
        this.originalResponse = that.originalResponse;
		return this;
	}
}
