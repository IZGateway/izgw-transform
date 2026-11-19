package gov.cdc.izgateway.xform.repository;

import java.util.Set;
import java.util.UUID;

/**
 * Simple CRUD interface for the storage of configuration entities.
 *
 * @param <T> the entity type
 */
public interface XformRepository<T> {
    /**
     * Gets an entity by its identifier.
     *
     * @param id the entity identifier
     * @return the entity, or null if it does not exist
     */
    public T getEntity(UUID id);
    /**
     * Gets all entities in the repository.
     *
     * @return the set of entities
     */
    public Set<T> getEntitySet();
    /**
     * Adds an entity to the repository.
     *
     * @param obj the entity to add
     */
    public void createEntity(T obj);
    /**
     * Replaces the stored entity that has the same identifier.
     *
     * @param obj the entity to update
     */
    public void updateEntity(T obj);
    /**
     * Deletes an entity by its identifier.
     *
     * @param id the entity identifier
     */
    public void deleteEntity(UUID id);
}
