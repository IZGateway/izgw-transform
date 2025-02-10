package gov.cdc.izgateway.xform.validator;

import gov.cdc.izgateway.xform.model.PreconditionInfo;
import gov.cdc.izgateway.xform.model.Solution;
import gov.cdc.izgateway.xform.model.SolutionOperation;
import gov.cdc.izgateway.xform.preconditions.Precondition;
import gov.cdc.izgateway.xform.services.PreconditionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TODO - look at @NonNullApi warnings
 */
@Component
public class SolutionValidator implements Validator {
    private final PreconditionService preconditionService;

    @Autowired
    public SolutionValidator(PreconditionService preconditionService) {
        this.preconditionService = preconditionService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Solution.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Solution solution = (Solution) target;

        if (solution.getRequestOperations() != null) {
            validateOperations(solution.getRequestOperations(), "requestOperations", errors);
        }

        if (solution.getResponseOperations() != null) {
            validateOperations(solution.getResponseOperations(), "responseOperations", errors);
        }
    }

    private void validateOperations(List<SolutionOperation> operations, String field, Errors errors) {
        for (int i = 0; i < operations.size(); i++) {
            SolutionOperation operation = operations.get(i);
            if (operation.getPreconditions() != null) {
                for (int j = 0; j < operation.getPreconditions().size(); j++) {
                    Precondition precondition = operation.getPreconditions().get(j);
                    if (!isValidPrecondition(precondition)) {
                        String path = String.format("%s[%d].preconditions[%d]", field, i, j);
                        errors.rejectValue(path, "invalid.precondition",
                                "Invalid precondition method: " + precondition.getMethod());
                    }
                }
            }
        }
    }

    private boolean isValidPrecondition(Precondition precondition) {
        return getValidPreconditionMethods().contains(precondition.getMethod());
    }

    private Set<String> getValidPreconditionMethods() {
        return preconditionService.getList().stream()
                .map(PreconditionInfo::getMethod)
                .collect(Collectors.toSet());
    }
}
