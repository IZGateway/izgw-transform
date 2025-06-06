package gov.cdc.izgateway.xform.services;

import gov.cdc.izgateway.security.principal.GroupToRoleMapper;
import gov.cdc.izgateway.xform.model.GroupRoleMapping;
import gov.cdc.izgateway.xform.repository.RepositoryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class GroupRoleMappingService extends GenericService<GroupRoleMapping> implements GroupToRoleMapper {

    @Autowired
    public GroupRoleMappingService(RepositoryFactory repositoryFactory) {
        super(repositoryFactory.groupRoleMappingRepository());
    }

    public List<String> getRolesByGroup(String group) {
        return repo.getEntitySet().stream()
                .filter(m -> m.getGroupName().equals(group))
                .findFirst()
                .map(GroupRoleMapping::getRoles)
                .orElse(Collections.emptyList());
    }

    /**
     * For the given groups, it returns the roles that are mapped to the groups.
     * @param groups The groups to map to roles
     * @return The roles that are mapped from the groups
     */
    @Override
    public Set<String> mapGroupsToRoles(Set<String> groups) {
        Set<String> roles = new HashSet<>();
        for (String group : groups) {
            roles.addAll(getRolesByGroup(group));
        }
        return roles;
    }

    @Override
    protected boolean isDuplicate(GroupRoleMapping groupRoleMapping) {
        return repo.getEntitySet().stream()
                .anyMatch(grp ->
                        grp.getGroupName().equalsIgnoreCase(groupRoleMapping.getGroupName())
                );
    }

}
