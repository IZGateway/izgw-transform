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

@Data 
@Schema(description="The Hub Request header in the SOAP message")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class XformHeader implements Serializable {
	private static final long serialVersionUID = 1L;

    @Schema(description="The transformed request.")
    private String transformedRequest;
    @Schema(description="The original response prior to transformation.")
    private String originalResponse;

	private static List<Pair<String, Function<SoapMessage, String>>> pairs = Arrays.asList(
            Pair.of("TransformedRequest", (Function<SoapMessage, String>)(m -> m.getXformHeader().getTransformedRequest())),
            Pair.of("OriginalResponse", (Function<SoapMessage, String>)(m -> m.getXformHeader().getOriginalResponse()))
		);
		
	@JsonIgnore
	public static List<Pair<String, Function<SoapMessage, String>>> getKeyValueSuppliers() {
		return pairs;
	}

	@JsonIgnore
	public boolean isEmpty() {
		return StringUtils.isAllEmpty(transformedRequest, originalResponse);
	}
	
	public XformHeader clear() {
		transformedRequest = originalResponse = null;
		return this;
	}

	public XformHeader copyFrom(XformHeader that) {
        this.transformedRequest = that.transformedRequest;
        this.originalResponse = that.originalResponse;
		return this;
	}
}