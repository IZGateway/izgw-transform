package gov.cdc.izgateway.xform.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

/**
 * Validator for the {@link ValidOrganization} annotation that checks that an organization identifier exists and is active.
 */
@Component
public class OrganizationValidator implements ConstraintValidator<ValidOrganization, UUID> {

    private final OrganizationValidationService validationService;

    /**
     * Creates the validator.
     * @param validationService the service that validates organizations
     */
    @Autowired
    public OrganizationValidator(OrganizationValidationService validationService) {
        this.validationService = validationService;
    }

    @Override
    public boolean isValid(UUID organizationId, ConstraintValidatorContext context) {
        return validationService.validateOrganization(organizationId, context);
    }
}