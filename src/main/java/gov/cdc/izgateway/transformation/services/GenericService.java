package gov.cdc.izgateway.transformation.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cdc.izgateway.transformation.model.BaseModel;
import gov.cdc.izgateway.transformation.model.ModelUtils;
import gov.cdc.izgateway.transformation.repository.TxFormRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

public abstract class GenericService<T extends BaseModel> implements TxFormService<T> {

    private final TxFormRepository<T> repo;

    protected GenericService(TxFormRepository<T> repo){
        this.repo = repo;
    }


    @Override
    public ResponseEntity<T> getObject(UUID id) {
        T entity = repo.getEntity(id);
        if ( entity == null ) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(entity, HttpStatus.OK);

    }

    @Override
    public ResponseEntity<String> getList(String nextCursor, String prevCursor, Boolean includeInactive, int limit) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> returnMap = new HashMap<>();
        int min = 0;
        int max = limit;
        String hasMore = "true";
        List<T> allEntityList = new ArrayList<>(repo.getEntitySet());

        List<T> filteredEntityList = ModelUtils.filterList(includeInactive, allEntityList);

        for (int i = 0; i < filteredEntityList.size(); i++){
            T newEntity = filteredEntityList.get(i);
            if(newEntity.getId().toString().equals(nextCursor)){
                min = i+1;
                max = i+limit+1;
            }
            else if(newEntity.getId().toString().equals(prevCursor)){
                min = i-limit-1;
                max = i;
            }
        }
        if (max > filteredEntityList.size()) {
            max = filteredEntityList.size();
            hasMore = "false";
        }

        if (min < 0) {
            min = 0;
            hasMore = "false";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        returnMap.put("data", filteredEntityList.subList(min, max));
        returnMap.put("has_more", hasMore);
        return new ResponseEntity<>(mapper.writeValueAsString(returnMap), headers, HttpStatus.OK);
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
