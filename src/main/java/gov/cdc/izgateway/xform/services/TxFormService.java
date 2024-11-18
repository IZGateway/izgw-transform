package gov.cdc.izgateway.xform.services;

import java.util.List;
import java.util.UUID;

public interface TxFormService<T> {
    T getObject(UUID id);
    List<T> getList();
    void update(T obj);
    void create(T obj);
    void delete(UUID id);
}
