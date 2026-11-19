package gov.cdc.izgateway.xform.validation;

import gov.cdc.izgateway.xform.model.Organization;
import gov.cdc.izgateway.xform.services.OrganizationService;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Checks that an organization exists and is active, and adds a constraint violation if not.
 */
@Service
public class OrganizationValidationService {

    private final OrganizationService organizationService;

    /**
     * Creates the service.
     * @param organizationService the service used to find organizations
     */
    @Autowired
    public OrganizationValidationService(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    /**
     * Checks that an organization exists and is active.
     * @param organizationId the identifier of the organization
     * @param context the validator context, used to add a constraint violation message
     * @return true if the organization exists and is active, false otherwise
     */
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