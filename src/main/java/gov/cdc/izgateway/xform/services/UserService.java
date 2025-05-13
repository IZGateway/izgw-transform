package gov.cdc.izgateway.xform.services;

import gov.cdc.izgateway.xform.model.User;
import gov.cdc.izgateway.xform.repository.XformRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService extends GenericService<User> {
    @Autowired
    public UserService(XformRepository<User> repo) {
        super(repo);
    }

    public User getUserByUserName(String userName) {
        return repo.getEntitySet().stream().filter(o -> o.getUserName().equals(userName) && Boolean.TRUE.equals(o.getActive())).findFirst().orElse(null);
    }

    @Override
    protected boolean isDuplicate(User user) {
        return repo.getEntitySet().stream()
                .anyMatch(u ->
                        u.getUserName().equalsIgnoreCase(user.getUserName())
                );
    }

}

