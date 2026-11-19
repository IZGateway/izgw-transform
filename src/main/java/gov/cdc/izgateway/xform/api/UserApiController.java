package gov.cdc.izgateway.xform.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import gov.cdc.izgateway.security.AccessControlRegistry;
import gov.cdc.izgateway.xform.model.User;
import gov.cdc.izgateway.xform.security.Roles;
import gov.cdc.izgateway.xform.services.UserService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.logging.Level;

/**
 * REST API for the users.
 */
@Log
@Lazy(false)
@RestController
public class UserApiController extends BaseApiController {

    private final UserService userService;

    /**
     * Creates the controller and registers it with the access control registry.
     *
     * @param userService the service for the users
     * @param registry the access control registry
     */
    @Autowired
    public UserApiController(
            UserService userService,
            AccessControlRegistry registry
    ) {
        super();
        this.userService = userService;
        registry.register(this);
    }

    /**
     * Gets one page of users.
     *
     * @param nextCursor the id of the user after which the page starts, or null
     * @param prevCursor the id of the user before which the page ends, or null
     * @param includeInactive if true, inactive users are included
     * @param limit the maximum number of users in the page
     * @return the page as JSON, or status 500 if the page cannot be serialized
     */
    @RolesAllowed({Roles.ADMIN})
    @GetMapping("/api/v1/users")
    public ResponseEntity<String> getUsersList(
            @RequestParam(required = false) String nextCursor,
            @RequestParam(required = false) String prevCursor,
            @RequestParam(defaultValue = "false") Boolean includeInactive,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            return processList(userService.getList(), nextCursor, prevCursor, includeInactive, limit);
        } catch (JsonProcessingException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gets one user.
     *
     * @param uuid the id of the user
     * @return the user, or status 404 if it is not found
     */
    @RolesAllowed({Roles.ADMIN})
    @GetMapping("/api/v1/users/{uuid}")
    public ResponseEntity<User> getUserByUUID(@PathVariable UUID uuid) {
        User entity = userService.getObject(uuid);
        if (entity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(entity, HttpStatus.OK);
    }

    /**
     * Replaces a user.
     *
     * @param uuid the id of the user
     * @param updatedUser the new user
     * @return the updated user
     */
    @RolesAllowed({Roles.ADMIN})
    @PutMapping("/api/v1/users/{uuid}")
    public ResponseEntity<User> updateUser(
            @PathVariable UUID uuid,
            @RequestBody @Valid User updatedUser
    ) {
        updatedUser.setId(uuid);
        userService.update(updatedUser);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    /**
     * Creates a user with a new id.
     *
     * @param user the user to create
     * @return the created user
     */
    @RolesAllowed({Roles.ADMIN})
    @PostMapping("/api/v1/users")
    public ResponseEntity<User> createUser(
            @Valid @RequestBody() User user
    ) {
        userService.create(user);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

}
