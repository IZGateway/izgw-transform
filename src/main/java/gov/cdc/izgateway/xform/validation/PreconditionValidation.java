package gov.cdc.izgateway.xform.validation;

import gov.cdc.izgateway.xform.exceptions.InvalidPreconditionException;
import gov.cdc.izgateway.xform.model.PreconditionInfo;
import gov.cdc.izgateway.xform.model.Solution;
import gov.cdc.izgateway.xform.model.SolutionOperation;
import gov.cdc.izgateway.xform.preconditions.Precondition;
import gov.cdc.izgateway.xform.services.PreconditionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Currently used in SolutionPreconditionValidator as part of validating Solutions posted via API
 * TODO - work in progress could likely be incorporated into SolutionPreconditionValidator or at the
 * very least moved to validator package.
 * TODO - rename to SolutionValidator and validate precondition and operations
 */
@Service
public class PreconditionValidation {
    private final PreconditionService preconditionService;

    @Autowired
    public PreconditionValidation(PreconditionService preconditionService) {
        this.preconditionService = preconditionService;
    }

    /**
     * Validates all preconditions in a solution against the available precondition methods
     * @param solution The Solution we want to validate
     * @throws InvalidPreconditionException if any preconditions are invalid
     */
    public void validateSolutionPreconditions(Solution solution) {
        Set<String> validPreconditionMethods = getValidPreconditionMethods();
        Set<String> invalidPreconditions = new HashSet<>();

        if (solution.getRequestOperations() != null) {
            validateOperationsList(solution.getRequestOperations(), validPreconditionMethods, invalidPreconditions);
        }

        if (solution.getResponseOperations() != null) {
            validateOperationsList(solution.getResponseOperations(), validPreconditionMethods, invalidPreconditions);
        }

        if (!invalidPreconditions.isEmpty()) {
            throw new InvalidPreconditionException("Invalid precondition methods found: " +
                    String.join(", ", invalidPreconditions));
        }
    }

    /**
     * Validates a specific precondition against the available precondition methods
     * @param precondition The precondition to validate
     * @return true if valid, false if invalid
     */
    public boolean isValidPrecondition(Precondition precondition) {
        return getValidPreconditionMethods().contains(precondition.getMethod());
    }

    private Set<String> getValidPreconditionMethods() {
        return preconditionService.getList().stream()
                .map(PreconditionInfo::getMethod)
                .collect(Collectors.toSet());
    }

    private void validateOperationsList(
            List<SolutionOperation> operations,
            Set<String> validPreconditionMethods,
            Set<String> invalidPreconditions
    ) {
        for (SolutionOperation operation : operations) {
            if (operation.getPreconditions() != null) {
                for (Precondition precondition : operation.getPreconditions()) {
                    String methodName = precondition.getMethod();
                    if (!validPreconditionMethods.contains(methodName)) {
                        invalidPreconditions.add(methodName);
                    }
                }
            }
        }
    }
}
