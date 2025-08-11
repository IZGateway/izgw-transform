package gov.cdc.izgateway.xform.repository.dynamodb.migration;

import gov.cdc.izgateway.xform.model.BaseModel;

public interface EntityTruncator<T extends BaseModel> {

    void truncate();

    Class<T> getEntityType();

    String getEntityName();
}
