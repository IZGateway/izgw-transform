package gov.cdc.izgateway.xform.logging;

import lombok.Data;

@Data
public class XformApiCrudDetail {
    private String eventId;
    private String userName;
    private String object;
    private String objectId;
    private String method;
    private String principalType;
    private Object oldData;
    private Object newData;
}
