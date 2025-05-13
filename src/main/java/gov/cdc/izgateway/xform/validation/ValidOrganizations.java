package gov.cdc.izgateway.xform.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Sets up a Validation Annotation for Organizations. Meant to be used, for example,
 * in the Pipeline API so that when the caller specifies an organizationId we can check
 * to see if that Organization exists in the system and is active.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OrganizationsValidator.class)
@Documented
public @interface ValidOrganizations {
    String message() default "Organization does not exist";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
