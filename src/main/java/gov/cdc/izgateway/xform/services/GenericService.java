package gov.cdc.izgateway.xform.services;

import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.security.IzgPrincipal;
import gov.cdc.izgateway.xform.logging.ApiEventLogger;
import gov.cdc.izgateway.xform.model.BaseModel;
import gov.cdc.izgateway.xform.model.Organization;
import gov.cdc.izgateway.xform.model.OrganizationAware;
import gov.cdc.izgateway.xform.repository.XformRepository;
import gov.cdc.izgateway.xform.security.Roles;
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

        if (!isAccessible(existing)) {
            return null;
        }

        ApiEventLogger.logReadEvent(existing);

        return existing;
    }

    @Override
    public List<T> getList() {
        ArrayList<T> list = new ArrayList<>(repo.getEntitySet());
        list.removeIf(item -> !isAccessible(item));

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

        // If object is organization, skip the organizationId check
        // otherwise, check if the user has access to the organization ID
        if (!(obj instanceof Organization) && !isAccessible(obj)) {
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
        if (!isAccessible(item)) {
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
     * Check if the object is accessible based on its organization ID or if the user is an admin.
     */
    private boolean isAccessible(T obj) {
        // If the RequestContext principal is an admin, return true
        if (RequestContext.getPrincipal().getRoles().contains(Roles.ADMIN))  {
            return true;
        }

        if (obj instanceof OrganizationAware organizationAware) {
            Set<UUID> allowedOrgIds = getAllowedOrganizationIds();
            return allowedOrgIds.contains(organizationAware.getOrganizationId());
        }

        return true; // If the object is not organization-aware, it's accessible
    }

    /**
     * Check if the object would be a duplicate.
     * This needs to be overridden by subclasses for their specific needs.
     * @param obj The object to check for duplication
     * @return true if the object would be a duplicate, false otherwise
     */
    protected abstract boolean isDuplicate(T obj);
}
