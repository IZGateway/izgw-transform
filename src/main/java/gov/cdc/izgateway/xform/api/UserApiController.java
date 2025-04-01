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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.logging.Level;

@Log
@Lazy(false)
@RestController
public class UserApiController extends BaseApiController {

    private final UserService userService;

    @Value("${xform.allow-delete-via-api}")
    private Boolean allowDelete;

    @Autowired
    public UserApiController(
            UserService userService,
            AccessControlRegistry registry
    ) {
        super();
        this.userService = userService;
        registry.register(this);
    }

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

    @RolesAllowed({Roles.ADMIN})
    @GetMapping("/api/v1/users/{uuid}")
    public ResponseEntity<User> getUserByUUID(@PathVariable UUID uuid) {
        User entity = userService.getObject(uuid);
        if (entity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(entity, HttpStatus.OK);
    }

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

    @RolesAllowed({Roles.ADMIN})
    @PostMapping("/api/v1/users")
    public ResponseEntity<User> createUser(
            @Valid @RequestBody() User user
    ) {
        userService.create(user);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @RolesAllowed({Roles.ADMIN})
    @DeleteMapping("/api/v1/users/{uuid}")
    public ResponseEntity<User> deleteUser(
            @PathVariable UUID uuid
    ) {
        if (Boolean.FALSE.equals(allowDelete)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        userService.delete(uuid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
