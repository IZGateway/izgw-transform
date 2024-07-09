package gov.cdc.izgateway.transformation.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cdc.izgateway.transformation.model.Pipeline;
import gov.cdc.izgateway.transformation.repository.PipelineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PipelineService {
    private final PipelineRepository repo;

    @Autowired
    public PipelineService(PipelineRepository repo) {
        this.repo = repo;
    }

    public ResponseEntity<Pipeline> getPipelineResponse(UUID id){
        Pipeline pipeline = repo.getPipeline(id);
        if ( pipeline == null ) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(pipeline, HttpStatus.OK);
    }

    public ResponseEntity<String> getPipelineList(String nextCursor, String prevCursor, Boolean includeInactive, int limit) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> returnMap = new HashMap<>();
        int min = 0;
        int max = limit;
        String hasMore = "true";
        List<Pipeline> allPipelineList = new ArrayList<>(repo.getPipelineSet());

        List<Pipeline> filteredPipelineList = filterList(includeInactive, allPipelineList);

        for (int i = 0; i < filteredPipelineList.size(); i++){
            Pipeline newPipeline = filteredPipelineList.get(i);
            if(newPipeline.getId().toString().equals(nextCursor)){
                min = i+1;
                max = i+limit+1;
            }
            else if(newPipeline.getId().toString().equals(prevCursor)){
                min = i-limit-1;
                max = i;
            }
        }
        if (max > filteredPipelineList.size()) {
            max = filteredPipelineList.size();
            hasMore = "false";
        }

        if (min < 0) {
            min = 0;
            hasMore = "false";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        returnMap.put("data", filteredPipelineList.subList(min, max));
        returnMap.put("has_more", hasMore);
        return new ResponseEntity<>(mapper.writeValueAsString(returnMap), headers, HttpStatus.OK);

    }

    public void updatePipeline(Pipeline pipeline) {
        Pipeline existingPipeline = repo.getPipeline(pipeline.getId());

        if (existingPipeline == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pipeline not found");
        }

        repo.updatePipeline(pipeline);
    }

    public void createPipeline(Pipeline pipeline) {
        Pipeline existingPipeline = repo.getPipeline(pipeline.getId());

        if (existingPipeline != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Pipeline already exists");
        }

        repo.createPipeline(pipeline);
    }

    private static List<Pipeline> filterList(Boolean includeInactive, List<Pipeline> allPipelineList) {
        if (Boolean.FALSE.equals(includeInactive)) {
            return allPipelineList.stream()
                    .filter(Pipeline::getActive)
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>(allPipelineList);
        }
    }

}
