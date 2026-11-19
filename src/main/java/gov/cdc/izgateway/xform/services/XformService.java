package gov.cdc.izgateway.xform.services;

import java.util.List;
import java.util.UUID;

/**
 * Basic operations of a service for configuration entities.
 *
 * @param <T> the entity type
 */
public interface XformService<T> {
    /**
     * Gets one entity.
     *
     * @param id the id of the entity
     * @return the entity, or null if it is not found or not accessible
     */
    T getObject(UUID id);
    /**
     * Gets all accessible entities.
     *
     * @return the entities
     */
    List<T> getList();
    /**
     * Replaces an entity.
     *
     * @param obj the new entity
     */
    void update(T obj);
    /**
     * Creates an entity.
     *
     * @param obj the entity to create
     */
    void create(T obj);
    /**
     * Deletes an entity.
     *
     * @param id the id of the entity
     */
    void delete(UUID id);
}
