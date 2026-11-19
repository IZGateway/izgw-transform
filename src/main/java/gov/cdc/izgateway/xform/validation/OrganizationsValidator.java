package gov.cdc.izgateway.xform.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

/**
 * Validator for the {@link ValidOrganizations} annotation that checks that each organization in a set of identifiers exists and is active.
 */
@Component
public class OrganizationsValidator implements ConstraintValidator<ValidOrganizations, Set<UUID>> {

    private final OrganizationValidationService validationService;

    /**
     * Creates the validator.
     * @param validationService the service that validates organizations
     */
    @Autowired
    public OrganizationsValidator(OrganizationValidationService validationService) {
        this.validationService = validationService;
    }

    @Override
    public boolean isValid(Set<UUID> organizationIds, ConstraintValidatorContext context) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return false;
        }

        for (UUID organizationId : organizationIds) {
            if (!validationService.validateOrganization(organizationId, context)) {
                return false;
            }
        }

        return true;
    }
}