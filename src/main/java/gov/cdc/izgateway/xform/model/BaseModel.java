package gov.cdc.izgateway.xform.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.util.UUID;

public abstract class BaseModel {
    public abstract UUID getId();
    public abstract void setId(UUID id);
    public abstract Boolean getActive();

    /** The attribute to use for entity type */
    public static final String ENTITY_TYPE = "entityType";
    /** The attribute to use for the sort key */
    public static final String SORT_KEY = "sortKey";


    @JsonIgnore
    @DynamoDbPartitionKey
    @DynamoDbAttribute(ENTITY_TYPE)
    public String getEntityType() {
        return getClass().getSimpleName();
    }

    public void setEntityType(String entityType) {
        // Read-only attribute
    }

    @JsonIgnore
    @DynamoDbSortKey
    @DynamoDbAttribute(SORT_KEY)
    public String getSortKey() {
        return getId() != null ? getId().toString() : null;
    }

    public void setSortKey(String sortKey) {
        setId(sortKey != null ? UUID.fromString(sortKey) : null);
    }
}
