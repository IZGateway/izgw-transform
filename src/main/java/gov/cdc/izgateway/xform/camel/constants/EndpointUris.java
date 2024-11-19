package gov.cdc.izgateway.xform.camel.constants;

public class EndpointUris {

    // The URI for the Xform Service endpoint compatible with the IZ Gateway Hub WSDL
    public static final String IZGTS_IISHubService = "izgts:IISHubService";

    // The URI for the Xform Service endpoint compatible with the IIS CDC WSDL
    public static final String IZGTS_IISService = "izgts:IISService";

    // The URI to place the incoming message onto the Camel route for IZG Hub pipelines
    public static final String DIRECT_HUB_PIPELINE = "direct:izghubTransformerPipeline";

    // The URI to place the incoming message onto the Camel route for IIS pipelines
    public static final String DIRECT_IIS_PIPELINE = "direct:iisTransformerPipeline";

    // The URI for the IZ Gateway Hub service endpoint
    public static final String IZGHUB_IISHubService = "izghub:IISHubService";

    // The URI for the IIS service endpoint
    public static final String IIS_IISService = "iis:IISService";
}
