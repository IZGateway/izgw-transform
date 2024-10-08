package gov.cdc.izgateway.transformation.services;

import gov.cdc.izgateway.transformation.model.BaseModel;
import gov.cdc.izgateway.transformation.repository.TxFormRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

public abstract class GenericService<T extends BaseModel> implements TxFormService<T> {

    protected final TxFormRepository<T> repo;

    protected GenericService(TxFormRepository<T> repo){
        this.repo = repo;
    }

    @Override
    public T getObject(UUID id) {
        return repo.getEntity(id);
    }

    @Override
    public List<T> getList() {
        return new ArrayList<>(repo.getEntitySet());
    }

    @Override
    public void update(T obj) {
        T existing = repo.getEntity(obj.getId());

        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found");
        }

        repo.updateEntity(obj);
    }

    @Override
    public void create(T obj) {
        T existing = repo.getEntity(obj.getId());

        if (existing != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Item already exists");
        }

        repo.createEntity(obj);
    }

    @Override
    public void delete(UUID id) {
        T solution = repo.getEntity(id);
        if (solution == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found");
        }
        repo.deleteEntity(id);
    }

}
