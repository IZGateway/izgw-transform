package gov.cdc.izgateway.transformation.services;

import gov.cdc.izgateway.transformation.model.Organization;
import gov.cdc.izgateway.transformation.model.User;
import gov.cdc.izgateway.transformation.repository.TxFormRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService extends GenericService<User> {
    @Autowired
    public UserService(TxFormRepository<User> repo) {
        super(repo);
    }

    public User getUserByUserName(String userName) {
        return repo.getEntitySet().stream().filter(o -> o.getUserName().equals(userName) && Boolean.TRUE.equals(o.getActive())).findFirst().orElse(null);
    }

}

