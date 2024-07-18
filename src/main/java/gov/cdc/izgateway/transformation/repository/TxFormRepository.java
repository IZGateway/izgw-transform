package gov.cdc.izgateway.transformation.repository;

import java.util.Set;
import java.util.UUID;

public interface TxFormRepository<T> {
    public T getEntity(UUID id);
    public Set<T> getEntitySet();
    public void createEntity(T obj);
    public void updateEntity(T obj);
    public void deleteEntity(UUID id);
}
