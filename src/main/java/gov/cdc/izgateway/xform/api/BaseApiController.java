package gov.cdc.izgateway.xform.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cdc.izgateway.xform.model.BaseModel;
import lombok.extern.java.Log;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class BaseApiController {

    protected <T extends BaseModel> ResponseEntity<String> processList(List<T> allEntityList, String nextCursor, String prevCursor, Boolean includeInactive, int limit) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> returnMap = new HashMap<>();
        int min = 0;
        int max = limit;
        String hasMore = "true";

        List<T> filteredEntityList = filterList(includeInactive, allEntityList);

        for (int i = 0; i < filteredEntityList.size(); i++) {
            T newEntity = filteredEntityList.get(i);
            if (newEntity.getId().toString().equals(nextCursor)) {
                min = i + 1;
                max = i + limit + 1;
            } else if (newEntity.getId().toString().equals(prevCursor)) {
                min = i - limit - 1;
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

    private <T extends BaseModel> List<T> filterList(Boolean includeInactive, List<T> allList) {
        if (Boolean.FALSE.equals(includeInactive)) {
            return allList.stream()
                    .filter(T::getActive)
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>(allList);
        }
    }
}
