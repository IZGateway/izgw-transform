package gov.cdc.izgateway.transformation.services;

import gov.cdc.izgateway.transformation.model.User;
import gov.cdc.izgateway.transformation.repository.TxFormRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class XformAccessControlService extends GenericService<User> {
    @Autowired
    public XformAccessControlService(TxFormRepository<User> repo) {
        super(repo);
    }

    public boolean userExists(String userName) {
        return repo.getEntitySet().stream().anyMatch(u -> u.getUserName().equals(userName) && Boolean.TRUE.equals(u.getActive()));
    }
}
