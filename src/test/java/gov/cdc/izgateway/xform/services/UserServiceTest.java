package gov.cdc.izgateway.xform.services;

import gov.cdc.izgateway.xform.model.User;
import gov.cdc.izgateway.xform.repository.RepositoryFactory;
import gov.cdc.izgateway.xform.repository.XformRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UserService}, focusing on the {@code getUserByUserName}
 * method.
 *
 * <p>
 * Tests cover the following scenarios:
 * <ul>
 * <li>Exact username match</li>
 * <li>Case-insensitive matching between stored and JWT-supplied usernames</li>
 * <li>Inactive users are excluded from results</li>
 * <li>No matching user returns {@code null}</li>
 * <li>Empty repository returns {@code null}</li>
 * </ul>
 *
 * <p>
 * The {@link XformRepository} and {@link RepositoryFactory} dependencies are
 * mocked
 * via Mockito so that tests remain isolated from any persistence layer.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    /** Mocked repository that provides access to {@link User} entities. */
    @Mock
    private XformRepository<User> userRepository;

    /**
     * Mocked factory used to supply the {@link #userRepository} to the service
     * under test.
     */
    @Mock
    private RepositoryFactory repositoryFactory;

    /**
     * The service instance under test, constructed with the mocked
     * {@link #repositoryFactory}.
     */
    private UserService userService;

    /**
     * Initialises the {@link UserService} before each test, wiring it to the mocked
     * {@link RepositoryFactory} so that calls to
     * {@code repositoryFactory.userRepository()}
     * return the mocked {@link #userRepository}.
     */
    @BeforeEach
    void setUp() {
        when(repositoryFactory.userRepository()).thenReturn(userRepository);
        userService = new UserService(repositoryFactory);
    }

    // ---------------------------------------------------------------------------
    // Helper factory methods
    // ---------------------------------------------------------------------------

    /**
     * Creates an active {@link User} with the given username and a random UUID.
     *
     * @param userName the username to assign to the user
     * @return a fully initialised, active {@link User}
     */
    private User activeUser(String userName) {
        User user = new User(userName);
        user.setId(UUID.randomUUID());
        user.setActive(true);
        return user;
    }

    /**
     * Creates an inactive {@link User} with the given username and a random UUID.
     *
     * @param userName the username to assign to the user
     * @return a fully initialised, inactive {@link User}
     */
    private User inactiveUser(String userName) {
        User user = new User(userName);
        user.setId(UUID.randomUUID());
        user.setActive(false);
        return user;
    }

    // ---------------------------------------------------------------------------
    // Tests: getUserByUserName
    // ---------------------------------------------------------------------------

    /**
     * Verifies that an exact, case-identical username match returns the expected
     * active user.
     */
    @Test
    void getUserByUserName_exactMatch_returnsUser() {
        when(userRepository.getEntitySet()).thenReturn(new LinkedHashSet<>(Set.of(activeUser("cooluser@ainq.com"))));

        User result = userService.getUserByUserName("cooluser@ainq.com");

        assertNotNull(result);
        assertEquals("cooluser@ainq.com", result.getUserName());
    }

    /**
     * Verifies that lookup succeeds when the username is stored in lowercase but
     * the
     * JWT-supplied username uses mixed case — i.e., matching is case-insensitive.
     */
    @Test
    void getUserByUserName_storedLowercase_jwtMixedCase_returnsUser() {
        when(userRepository.getEntitySet()).thenReturn(new LinkedHashSet<>(Set.of(activeUser("cooluser@ainq.com"))));

        User result = userService.getUserByUserName("CoolUser@ainq.com");

        assertNotNull(result);
    }

    /**
     * Verifies that lookup succeeds when the username is stored in mixed case but
     * the
     * JWT-supplied username is lowercase — i.e., matching is case-insensitive.
     */
    @Test
    void getUserByUserName_storedMixedCase_jwtLowercase_returnsUser() {
        when(userRepository.getEntitySet()).thenReturn(new LinkedHashSet<>(Set.of(activeUser("CoolUser@ainq.com"))));

        User result = userService.getUserByUserName("cooluser@ainq.com");

        assertNotNull(result);
    }

    /**
     * Verifies that lookup succeeds when the username is stored in all uppercase
     * but the
     * JWT-supplied username is lowercase — i.e., matching is case-insensitive.
     */
    @Test
    void getUserByUserName_storedUppercase_jwtLowercase_returnsUser() {
        when(userRepository.getEntitySet()).thenReturn(new LinkedHashSet<>(Set.of(activeUser("COOLUSER@AINQ.COM"))));

        User result = userService.getUserByUserName("cooluser@ainq.com");

        assertNotNull(result);
    }

    /**
     * Verifies that an exact username match for an <em>inactive</em> user returns
     * {@code null},
     * ensuring that inactive accounts cannot authenticate.
     */
    @Test
    void getUserByUserName_inactiveUser_returnsNull() {
        when(userRepository.getEntitySet()).thenReturn(new LinkedHashSet<>(Set.of(inactiveUser("cooluser@ainq.com"))));

        User result = userService.getUserByUserName("cooluser@ainq.com");

        assertNull(result);
    }

    /**
     * Verifies that a case-insensitive match against an <em>inactive</em> user
     * still returns
     * {@code null}, confirming that inactive filtering is applied regardless of
     * case.
     */
    @Test
    void getUserByUserName_inactiveUser_caseInsensitiveMatch_returnsNull() {
        when(userRepository.getEntitySet()).thenReturn(new LinkedHashSet<>(Set.of(inactiveUser("cooluser@ainq.com"))));

        User result = userService.getUserByUserName("CoolUser@ainq.com");

        assertNull(result);
    }

    /**
     * Verifies that {@code null} is returned when no user in the repository matches
     * the requested username.
     */
    @Test
    void getUserByUserName_noMatchingUser_returnsNull() {
        when(userRepository.getEntitySet()).thenReturn(new LinkedHashSet<>(Set.of(activeUser("jdoe@ainq.com"))));

        User result = userService.getUserByUserName("cooluser@ainq.com");

        assertNull(result);
    }

    /**
     * Verifies that {@code null} is returned when the repository contains no users
     * at all.
     */
    @Test
    void getUserByUserName_emptyRepository_returnsNull() {
        when(userRepository.getEntitySet()).thenReturn(new LinkedHashSet<>());

        User result = userService.getUserByUserName("cooluser@ainq.com");

        assertNull(result);
    }
}
