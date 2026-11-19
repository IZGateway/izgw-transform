package gov.cdc.izgateway.xform.logging;

import lombok.Data;

/**
 * Details of an API create, update or delete event that {@link ApiEventLogger} writes to the API log.
 */
@Data
public class XformApiDetail {
    private String eventId;
    private String userName;
    private String object;
    private String objectId;
    private String method;
    private String principalType;
    private Object oldData;
    private Object newData;

    /**
     * Creates an empty API event detail.
     */
    public XformApiDetail() {
        // Lombok generates the accessors.
    }
}
