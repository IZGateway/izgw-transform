package gov.cdc.izgateway.transformation.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface TxFormService<T> {
    T getObject(UUID id);
    ResponseEntity<String> getList(String nextCursor, String prevCursor, Boolean includeInactive, int limit) throws JsonProcessingException;
    void update(T obj);
    void create(T obj);
    void delete(UUID id);
}
