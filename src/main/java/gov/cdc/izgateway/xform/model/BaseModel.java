package gov.cdc.izgateway.xform.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.util.UUID;

/**
 * Base class for the configuration entities that are stored in files or in DynamoDB.
 */
public abstract class BaseModel {
    /**
     * Returns the unique identifier of the entity.
     *
     * @return the identifier
     */
    public abstract UUID getId();
    /**
     * Sets the unique identifier of the entity.
     *
     * @param id the identifier
     */
    public abstract void setId(UUID id);
    /**
     * Returns true if the entity is active.
     *
     * @return the active flag
     */
    public abstract Boolean getActive();

    /** The attribute to use for entity type */
    public static final String ENTITY_TYPE = "entityType";
    /** The attribute to use for the sort key */
    public static final String SORT_KEY = "sortKey";

    /**
     * Creates the entity.
     */
    public BaseModel() {
    }

    /**
     * Returns the DynamoDB partition key, which is the simple class name of the entity.
     *
     * @return the entity type
     */
    @JsonIgnore
    @DynamoDbPartitionKey
    @DynamoDbAttribute(ENTITY_TYPE)
    public String getEntityType() {
        return getClass().getSimpleName();
    }

    /**
     * Does nothing, because the entity type is read-only. DynamoDB needs a setter for the attribute.
     *
     * @param entityType ignored
     */
    public void setEntityType(String entityType) {
        // Read-only attribute
    }

    /**
     * Returns the DynamoDB sort key, which is the identifier as a string.
     *
     * @return the sort key, or null if the identifier is null
     */
    @JsonIgnore
    @DynamoDbSortKey
    @DynamoDbAttribute(SORT_KEY)
    public String getSortKey() {
        return getId() != null ? getId().toString() : null;
    }

    /**
     * Sets the identifier from the DynamoDB sort key.
     *
     * @param sortKey the identifier as a string, or null
     */
    public void setSortKey(String sortKey) {
        setId(sortKey != null ? UUID.fromString(sortKey) : null);
    }
}
