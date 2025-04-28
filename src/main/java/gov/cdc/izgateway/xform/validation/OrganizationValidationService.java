package gov.cdc.izgateway.xform.validation;

import gov.cdc.izgateway.xform.model.Organization;
import gov.cdc.izgateway.xform.services.OrganizationService;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrganizationValidationService {

    private final OrganizationService organizationService;

    @Autowired
    public OrganizationValidationService(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    public boolean validateOrganization(UUID organizationId, ConstraintValidatorContext context) {
        if (organizationId == null) {
            return false;
        }

        Organization organization = organizationService.getObject(organizationId);

        if (organization == null) {
            addConstraintViolation(context, "Organization not found with ID " + organizationId);
            return false;
        }

        if (Boolean.FALSE.equals(organization.getActive())) {
            addConstraintViolation(context, "Organization with ID " + organizationId + " is NOT active");
            return false;
        }

        return true;
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}