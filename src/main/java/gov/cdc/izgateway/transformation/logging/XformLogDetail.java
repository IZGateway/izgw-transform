package gov.cdc.izgateway.transformation.logging;

import lombok.Data;

@Data
public class XformLogDetail {
    public String eventId;
    public String concept;
    public boolean processError;
    public String name;
}
