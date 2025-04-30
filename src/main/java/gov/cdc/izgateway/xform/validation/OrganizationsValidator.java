package gov.cdc.izgateway.xform.validation;

import gov.cdc.izgateway.xform.model.Organization;
import gov.cdc.izgateway.xform.services.OrganizationService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
public class OrganizationsValidator implements ConstraintValidator<ValidOrganizations, Set<UUID>> {

    private final OrganizationValidationService validationService;

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