package gov.cdc.izgateway.xform.services;

import gov.cdc.izgateway.model.IAccessControl;
import gov.cdc.izgateway.service.IAccessControlService;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Noop implementation of the IAccessControlService interface
 * This is needed until we refactor the core class "AccessController" as it requires an IAccessControlService
 * implementation.  This AccessControlServiceNoop class is not used by Xform Service.
 */
@Component
public class AccessControlServiceNoop implements IAccessControlService {

    @Override
    public void refresh() {
    	// Noop does nothing
    }

    @Override
    public Map<String, TreeSet<String>> getUserRoles() {
        return Map.of();
    }

    @Override
    public Map<String, Object> getGroups() {
        return Map.of();
    }

    @Override
    public boolean isUserInRole(String user, String role) {
        return false;
    }

    @Override
    public boolean isUserDenied(String user) {
        return false;
    }

    @Override
    public Set<String> getEventTypes() {
        return Set.of();
    }

    @Override
    public List<String> getAllowedRoles(RequestMethod method, String path) {
        return List.of();
    }

    @Override
    public Boolean checkAccess(String user, String method, String path) {
        return false;
    }

    @Override
    public boolean isUserInGroup(String user, String group) {
        return false;
    }

    @Override
    public IAccessControl removeUserFromDenyList(String user) {
        return null;
    }

    @Override
    public IAccessControl addUserToDenyList(String user) {
        return null;
    }

	@Override
	public boolean canAccessDestination(String user, String group) {
		return false;
	}

	@Override
	public Set<String> getDenyList() {
		return Set.of();
	}
}
