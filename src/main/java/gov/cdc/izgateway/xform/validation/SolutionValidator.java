package gov.cdc.izgateway.xform.validation;

import gov.cdc.izgateway.xform.model.Pipe;
import gov.cdc.izgateway.xform.model.Solution;
import gov.cdc.izgateway.xform.services.SolutionService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class SolutionValidator implements ConstraintValidator<ValidSolution, Pipe> {
    private final SolutionService solutionService;

    @Autowired
    public SolutionValidator(SolutionService solutionService) {
        this.solutionService = solutionService;
    }

    @Override
    public boolean isValid(Pipe pipe, ConstraintValidatorContext context) {
        if (pipe.getSolutionId() == null || pipe.getSolutionVersion() == null) {
            return false;
        }

        Solution solution = solutionService.getObject(pipe.getSolutionId());

        // If a Solution is not found with the id then it is not valid
        // If the Solution exist, but not a Solution with the version then it is not valid
        if (solution == null || !solution.getVersion().equals(pipe.getSolutionVersion())) {
            context.disableDefaultConstraintViolation(); // we want to override the default validation message
            context.buildConstraintViolationWithTemplate(
                    "Solution not found with ID " + pipe.getSolutionId() + " and version " + pipe.getSolutionVersion())
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
