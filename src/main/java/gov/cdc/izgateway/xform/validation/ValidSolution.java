package gov.cdc.izgateway.xform.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validation annotation for a pipe that checks that its solution exists with the given version.
 */
@Documented
@Constraint(validatedBy = SolutionValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSolution {
    /**
     * Message to display when validation fails.
     * @return validation error message
     */
    String message() default "Invalid solution version";
    /**
     * Validation groups that this constraint belongs to.
     * @return the validation groups
     */
    Class<?>[] groups() default {};
    /**
     * Payload that clients can attach to this constraint.
     * @return the payload types
     */
    Class<? extends Payload>[] payload() default {};
}
