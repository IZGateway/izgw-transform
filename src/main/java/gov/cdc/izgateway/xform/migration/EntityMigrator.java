package gov.cdc.izgateway.xform.migration;

import gov.cdc.izgateway.xform.model.BaseModel;

/**
 * Interface for entity-specific migration logic from file storage to DynamoDB.
 *
 * @param <T> The entity type to migrate
 */
public interface EntityMigrator<T extends BaseModel> {
    
    /**
     * Performs the migration for this entity type.
     *
     * @return MigrationResult containing success status and details
     */
    MigrationResult migrate();
    
    /**
     * Gets the entity type this migrator handles.
     *
     * @return The Class of the entity type
     */
    Class<T> getEntityType();
    
    /**
     * Gets a human-readable name for this entity type.
     *
     * @return The entity name for logging purposes
     */
    String getEntityName();
}