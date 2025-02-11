package gov.cdc.izgateway.xform.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SolutionValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSolution {
    String message() default "Invalid solution version";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
