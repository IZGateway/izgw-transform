package gov.cdc.izgateway.transformation.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cdc.izgateway.transformation.model.ModelUtils;
import gov.cdc.izgateway.transformation.model.Solution;
import gov.cdc.izgateway.transformation.repository.SolutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SolutionService {
    private final SolutionRepository repo;

    @Autowired
    public SolutionService(SolutionRepository repo){
        this.repo = repo;
    }

    public ResponseEntity<Solution> getSolutionResponse(UUID id) {

        Solution solution = repo.getSolution(id);
        if ( solution == null ) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(solution, HttpStatus.OK);
    }

    public ResponseEntity<String> getSolutionList(String nextCursor, String prevCursor, Boolean includeInactive, int limit) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> returnMap = new HashMap<>();
        int min = 0;
        int max = limit;
        String hasMore = "true";
        List<Solution> allSolutionList = new ArrayList<>(repo.getSolutionSet());

        List<Solution> filteredSolutionList = ModelUtils.filterList(includeInactive, allSolutionList);

        for (int i = 0; i < filteredSolutionList.size(); i++){
            Solution newSolution = filteredSolutionList.get(i);
            if(newSolution.getId().toString().equals(nextCursor)){
                min = i+1;
                max = i+limit+1;
            }
            else if(newSolution.getId().toString().equals(prevCursor)){
                min = i-limit-1;
                max = i;
            }
        }
        if (max > filteredSolutionList.size()) {
            max = filteredSolutionList.size();
            hasMore = "false";
        }

        if (min < 0) {
            min = 0;
            hasMore = "false";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        returnMap.put("data", filteredSolutionList.subList(min, max));
        returnMap.put("has_more", hasMore);
        return new ResponseEntity<>(mapper.writeValueAsString(returnMap), headers, HttpStatus.OK);

    }
}
