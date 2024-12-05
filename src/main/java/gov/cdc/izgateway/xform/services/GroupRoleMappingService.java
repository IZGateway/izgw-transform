package gov.cdc.izgateway.xform.services;

import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.model.IAccessControl;
import gov.cdc.izgateway.security.AccessControlRegistry;
import gov.cdc.izgateway.security.principal.GroupToRoleMapper;
import gov.cdc.izgateway.service.IAccessControlService;
import gov.cdc.izgateway.xform.model.AccessControl;
import gov.cdc.izgateway.xform.model.GroupRoleMapping;
import gov.cdc.izgateway.xform.repository.XformRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.*;

@Slf4j
@Service
public class GroupRoleMappingService extends GenericService<GroupRoleMapping> implements GroupToRoleMapper {

    @Autowired
    public GroupRoleMappingService(XformRepository<GroupRoleMapping> repo) {
        super(repo);
    }

    public List<String> getRolesByGroup(String group) {
        return repo.getEntitySet().stream()
                .filter(m -> m.getGroupName().equals(group))
                .findFirst()
                .map(GroupRoleMapping::getRoles)
                .map(List::of)
                .orElse(Collections.emptyList());
    }

    /**
     * For the given groups, it returns the roles that are mapped to the groups.
     * @param groups
     * @return
     */
    @Override
    public Set<String> mapGroupsToRoles(Set<String> groups) {
        Set<String> roles = new HashSet<>();
        for (String group : groups) {
            roles.addAll(getRolesByGroup(group));
        }
        return roles;
    }
}

