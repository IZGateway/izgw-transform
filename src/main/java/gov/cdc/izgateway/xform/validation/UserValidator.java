package gov.cdc.izgateway.xform.validation;

import gov.cdc.izgateway.xform.model.User;
import gov.cdc.izgateway.xform.services.UserService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Validator for the {@link ValidUser} annotation.
 * Verifies that a User with the specified UUID exists and is active.
 */
@Component
public final class UserValidator implements ConstraintValidator<ValidUser, UUID> {
    
    private final UserService userService;

    /**
     * Constructor for UserValidator.
     * @param userService the user service to use for validation
     */
    @Autowired
    public UserValidator(final UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean isValid(final UUID userId, final ConstraintValidatorContext context) {
        if (userId == null) {
            return false;
        }

        User user = userService.getObject(userId);

        // If user not found via ID is not valid
        if (user == null) {
            context
                    .disableDefaultConstraintViolation(); // override default message
            context
                    .buildConstraintViolationWithTemplate(
                            "User not found with ID " + userId)
                    .addConstraintViolation();
            return false;
        }

        // We know the user exists, return the active setting
        // If active then it's valid. If inactive, then we consider invalid.
        if (Boolean.FALSE.equals(user.getActive())) {
            context
                    .disableDefaultConstraintViolation(); // override default message
            context
                    .buildConstraintViolationWithTemplate(
                            "User with ID " + userId + " is NOT active")
                    .addConstraintViolation();
            return false;
        } else {
            return true;
        }
    }
}