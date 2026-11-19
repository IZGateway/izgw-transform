package gov.cdc.izgateway.xform.services;

import gov.cdc.izgateway.xform.model.User;
import gov.cdc.izgateway.xform.repository.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for the users.
 */
@Service
public class UserService extends GenericService<User> {
    /**
     * Creates the service.
     *
     * @param repositoryFactory the factory that supplies the user repository
     */
    @Autowired
    public UserService(RepositoryFactory repositoryFactory) {
        super(repositoryFactory.userRepository());
    }

    /**
     * Finds the active user with a user name, ignoring case.
     *
     * @param userName the user name
     * @return the user, or null if no active user has the user name
     */
    public User getUserByUserName(String userName) {
        return repo.getEntitySet().stream().filter(o -> o.getUserName().equalsIgnoreCase(userName) && Boolean.TRUE.equals(o.getActive())).findFirst().orElse(null);
    }

    @Override
    protected boolean isDuplicate(User user) {
        return repo.getEntitySet().stream()
                .anyMatch(u ->
                        u.getUserName().equalsIgnoreCase(user.getUserName())
                );
    }

}
