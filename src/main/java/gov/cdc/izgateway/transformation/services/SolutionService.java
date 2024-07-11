package gov.cdc.izgateway.transformation.services;

import gov.cdc.izgateway.transformation.model.Pipeline;
import gov.cdc.izgateway.transformation.model.Solution;
import gov.cdc.izgateway.transformation.repository.SolutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SolutionService {
    private final SolutionRepository repo;

    @Autowired
    public SolutionService(SolutionRepository repo){
        this.repo = repo;
    }

    public ResponseEntity<Solution> getSolutionResponse(UUID id){
        Solution solution = repo.getSolution(id);
        if ( solution == null ) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(solution, HttpStatus.OK);
    }

}
