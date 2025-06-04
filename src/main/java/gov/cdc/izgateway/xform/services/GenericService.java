package gov.cdc.izgateway.xform.services;

import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.security.IzgPrincipal;
import gov.cdc.izgateway.xform.logging.ApiEventLogger;
import gov.cdc.izgateway.xform.model.BaseModel;
import gov.cdc.izgateway.xform.model.OrganizationAware;
import gov.cdc.izgateway.xform.repository.XformRepository;
import gov.cdc.izgateway.xform.security.XformPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Slf4j
public abstract class GenericService<T extends BaseModel> implements XformService<T> {

    protected final XformRepository<T> repo;

    protected GenericService(XformRepository<T> repo){
        this.repo = repo;
    }

    @Override
    public T getObject(UUID id) {
        T existing = repo.getEntity(id);

        // Check if the object's organization ID is in the allowed organization IDs
        if (existing instanceof OrganizationAware organizationAware && !getAllowedOrganizationIds().contains(organizationAware.getOrganizationId())) {
            return null;
        }

        ApiEventLogger.logReadEvent(existing);

        return existing;
    }

    @Override
    public List<T> getList() {
        ArrayList<T> list = new ArrayList<>(repo.getEntitySet());

        list.removeIf(item -> item instanceof OrganizationAware organizationAware && !getAllowedOrganizationIds().contains(organizationAware.getOrganizationId()));

        ApiEventLogger.logReadEvent(list);
        return list;
    }

    @Override
    public void update(T obj) {
        T existing = repo.getEntity(obj.getId());

        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found");
        }

        repo.updateEntity(obj);

        ApiEventLogger.logUpdateEvent(obj, existing);
    }

    @Override
    public void create(T obj) {

        // Check if the object's organization ID is in the allowed organization IDs
        if (obj instanceof OrganizationAware organizationAware && !getAllowedOrganizationIds().contains(organizationAware.getOrganizationId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to create this object");
        }

        UUID newId = UUID.randomUUID();
        obj.setId(newId);

        // Check for duplicates, fields to use will be set by object type
        if (isDuplicate(obj)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Item already exists");
        }

        repo.createEntity(obj);

        ApiEventLogger.logCreateEvent(obj);
    }

    @Override
    public void delete(UUID id) {
        T item = repo.getEntity(id);
        if (item == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found");
        }

        // Check if the organization ID is in the allowed organization IDs
        if (item instanceof OrganizationAware organizationAware && !getAllowedOrganizationIds().contains(organizationAware.getOrganizationId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to delete this object");
        }

        repo.deleteEntity(id);

        ApiEventLogger.logDeleteEvent(item);
    }

    /*
     * This method is used to get the allowed organization IDs from the XformPrincipal.
     * It retrieves the principal from the request context and returns the allowed organization IDs.
     */
    private Set<UUID> getAllowedOrganizationIds() {
    	IzgPrincipal p = RequestContext.getPrincipal();
    	if (p instanceof XformPrincipal x) {
    		return x.getAllowedOrganizationIds();
    	}
    	return Collections.emptySet();
    }

    /**
     * Check if the object would be a duplicate.
     * This needs to be overridden by subclasses for their specific needs.
     * @param obj The object to check for duplication
     * @return true if the object would be a duplicate, false otherwise
     */
    protected abstract boolean isDuplicate(T obj);
}
