package gov.cdc.izgateway.xform.migration;

import gov.cdc.izgateway.xform.model.BaseModel;

/**
 * Interface for entity-specific migration from file storage to DynamoDB.
 *
 * There would be an implementation of this for each entity type (Organization, Pipeline, etc...)
 *
 * @param <T> Entity type to migrate
 */
public interface EntityMigrator<T extends BaseModel> {
    
    /**
     * Performs the migration for this entity type.
     * Each migrator handles its own logging and throws exceptions on failure.
     *
     * @return MigrationCounts with total, migrated, and skipped counts
     * @throws RuntimeException if migration fails
     */
    MigrationCounts migrate();
    
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
