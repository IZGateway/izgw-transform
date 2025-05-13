package gov.cdc.izgateway.xform.model;

import java.util.UUID;

public interface BaseModel {
    UUID getId();
    void setId(UUID id);
    Boolean getActive();
}
