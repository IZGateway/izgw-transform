package gov.cdc.izgateway.transformation.context;

import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.logging.info.DestinationInfo;
import gov.cdc.izgateway.logging.info.SourceInfo;
import gov.cdc.izgateway.transformation.enums.DataFlowDirection;
import gov.cdc.izgateway.transformation.enums.DataType;
import lombok.Data;

import java.util.HashMap;
import java.util.UUID;

@Data
public class XformContext<R, S> {
    private UUID organizationId;
    private String inboundEndpoint;
    private String outboundEndpoint;
    private R requestMessage;
    private S responseMessage;
    private DataFlowDirection currentDirection;
    private DataType dataType;
    private SourceInfo sourceInfo;
    private DestinationInfo destinationInfo;
    private HashMap<String, String> state;
}
