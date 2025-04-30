package gov.cdc.izgateway.xform.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sets up a Validation Annotation for Users. Meant to be used
 * to check if a specified userId exists in the system and is active.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UserValidator.class)
@Documented
public @interface ValidUser {
    /**
     * Message to display when validation fails.
     * @return validation error message
     */
    String message() default "User does not exist";
    
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
