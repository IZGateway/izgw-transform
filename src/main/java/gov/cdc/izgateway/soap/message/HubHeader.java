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

@Data 
@Schema(description="The Hub Request header in the SOAP message")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HubHeader implements HasDestinationUri, Serializable {
	private static final long serialVersionUID = 1L;
	@Schema(description="The destination to route the message to")
	private String destinationId;
	/**
	 * @deprecated
	 */
	@Schema(description="Additional destinations to route the message to (not used, present for backwards compatibility)")
	@Deprecated(since="2.0", forRemoval=true)
	private String additionalId;
	@Schema(description="The URI of the destination endpoint. Only used in response messages.")
	private String destinationUri;
//    @Schema(description="The transformed request.")
//    private String transformedRequest;
//    @Schema(description="The original response prior to transformation.")
//    private String originalResponse;

	private static List<Pair<String, Function<SoapMessage, String>>> pairs = Arrays.asList(
			Pair.of("DestinationId", (Function<SoapMessage, String>)(m -> m.getHubHeader().getDestinationId())),
			// Pair.of("AdditionalId", (Function<SoapMessage, String>)(m -> m.getHubHeader().getAdditionalId())),
			Pair.of("DestinationUri", (Function<SoapMessage, String>)(m -> m.getHubHeader().getDestinationUri()))
//            ,
//            Pair.of("TransformedRequest", (Function<SoapMessage, String>)(m -> m.getHubHeader().getTransformedRequest())),
//            Pair.of("OriginalResponse", (Function<SoapMessage, String>)(m -> m.getHubHeader().getOriginalResponse()))
		);
		
	@JsonIgnore
	public static List<Pair<String, Function<SoapMessage, String>>> getKeyValueSuppliers() {
		return pairs;
	}

	@JsonIgnore
	public boolean isEmpty() {
		return StringUtils.isAllEmpty(destinationId, destinationUri);
	}
	
	public HubHeader clear() {
        destinationId = destinationUri = additionalId = null;  // NOSONAR, yep 		destinationId = destinationUri = transformedRequest = originalResponse = additionalId = null;  // NOSONAR, yep it's deprecated
		return this;
	}

	public HubHeader copyFrom(HubHeader that) {
		this.destinationId = that.destinationId;
		this.additionalId = that.additionalId;	// NOSONAR, yep it's deprecated
		this.destinationUri = that.destinationUri;
//        this.transformedRequest = that.transformedRequest;
//        this.originalResponse = that.originalResponse;
		return this;
	}
}