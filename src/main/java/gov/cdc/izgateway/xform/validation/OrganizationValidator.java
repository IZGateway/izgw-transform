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
public class OrganizationValidator implements ConstraintValidator<ValidOrganization, UUID> {

    private final OrganizationValidationService validationService;

    @Autowired
    public OrganizationValidator(OrganizationValidationService validationService) {
        this.validationService = validationService;
    }

    @Override
    public boolean isValid(UUID organizationId, ConstraintValidatorContext context) {
        return validationService.validateOrganization(organizationId, context);
    }
}