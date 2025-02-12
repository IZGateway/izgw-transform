package gov.cdc.izgateway.xform.validation;

import gov.cdc.izgateway.xform.model.Organization;
import gov.cdc.izgateway.xform.services.OrganizationService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OrganizationValidator implements ConstraintValidator<ValidOrganization, UUID> {
    
    private final OrganizationService organizationService;

    @Autowired
    public OrganizationValidator(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @Override
    public boolean isValid(UUID organizationId, ConstraintValidatorContext context) {
        if (organizationId == null) {
            return false;
        }

        Organization organization = organizationService.getObject(organizationId);

        // If organization not found via ID is not valid
        if (organization == null) {
            context
                    .disableDefaultConstraintViolation(); // we want to override the default validation message
            context
                    .buildConstraintViolationWithTemplate("Organization not found with ID " + organizationId)
                    .addConstraintViolation();
            return false;
        }

        // We know the organization exists, return the active setting
        // If active then it's valid.  If in-active, then we consider in-valid.
        if (organization.getActive().equals(false)) {
            context
                    .disableDefaultConstraintViolation(); // we want to override the default validation message
            context
                    .buildConstraintViolationWithTemplate("Organization with ID " + organizationId + " is NOT active")
                    .addConstraintViolation();
            return false;
        } else {
            return true;
        }
    }
}
