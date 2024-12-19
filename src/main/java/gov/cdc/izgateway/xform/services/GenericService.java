package gov.cdc.izgateway.xform.services;

import gov.cdc.izgateway.xform.logging.ApiEventLogger;
//import gov.cdc.izgateway.xform.logging.XformApiLogDetail;
import gov.cdc.izgateway.xform.model.BaseModel;
import gov.cdc.izgateway.xform.repository.XformRepository;
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
        ApiEventLogger.logReadEvent(existing);

        return existing;
    }

    @Override
    public List<T> getList() {
        ArrayList<T> list = new ArrayList<>(repo.getEntitySet());
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
        T existing = repo.getEntity(obj.getId());

        if (existing != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Item already exists");
        }

        repo.createEntity(obj);

        ApiEventLogger.logCreateEvent(obj);
    }

    @Override
    public void delete(UUID id) {
        T solution = repo.getEntity(id);
        if (solution == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found");
        }
        repo.deleteEntity(id);

        ApiEventLogger.logDeleteEvent(solution);
    }
}
