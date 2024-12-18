package gov.cdc.izgateway.xform.services;

import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.logging.markers.Markers2;
import gov.cdc.izgateway.xform.logging.CrudEventLogger;
import gov.cdc.izgateway.xform.logging.XformApiCrudDetail;
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
        CrudEventLogger.logReadEvent(existing);

        return existing;
    }

    @Override
    public List<T> getList() {
        ArrayList<T> list = new ArrayList<>(repo.getEntitySet());
        CrudEventLogger.logReadEvent(list);
        return list;
    }

    @Override
    public void update(T obj) {
        T existing = repo.getEntity(obj.getId());

        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found");
        }

        repo.updateEntity(obj);

        CrudEventLogger.logUpdateEvent(obj, existing);
    }

    @Override
    public void create(T obj) {
        T existing = repo.getEntity(obj.getId());

        if (existing != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Item already exists");
        }

        repo.createEntity(obj);

        CrudEventLogger.logCreateEvent(obj);
    }

    @Override
    public void delete(UUID id) {
        T solution = repo.getEntity(id);
        if (solution == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found");
        }
        repo.deleteEntity(id);

        CrudEventLogger.logDeleteEvent(solution);
    }

//    private void logIt(T obj, T existing) {
//        XformApiCrudDetail data = new XformApiCrudDetail();
//        data.setEventId(RequestContext.getEventId());
//        data.setExistingData(existing);
//        data.setNewData(obj);
//        log.info(Markers2.append("crudLog", data), "Sample update log message!");
//    }
}
