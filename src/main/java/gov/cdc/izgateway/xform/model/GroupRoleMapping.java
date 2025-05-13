package gov.cdc.izgateway.xform.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import gov.cdc.izgateway.xform.security.Roles;

@Getter
@Setter
public class GroupRoleMapping implements BaseModel {
    private UUID id;
    @NotBlank(message = "GroupRoleMapping - Name is required")
    private String groupName;
    @NotNull(message = "GroupRoleMapping - Active status is required")
    private Boolean active;
    @NotNull(message = "GroupRoleMapping - Roles are required")
    private String[] roles;
    
    /**
     * Set the list of roles. This method validates the roles against
     * the set of legal roles and removes duplicates from the content.
     * If any invalid roles are detected, this method will throw an
     * IllegalArgumentException with an explanatory message.  Otherwise
     * it will set the roles to the duplicated list.  
     * @param roles	The list of roles to set.
     */
    public void setRoles(String[] roles) {
    	Set<String> invalidRoleSet = new TreeSet<>(Arrays.asList(roles));
    	// Normalize the list to a set
    	roles = invalidRoleSet.toArray(new String[0]);
    	invalidRoleSet.removeAll(Roles.ALL_ROLES);
    	if (!invalidRoleSet.isEmpty()) {
    		String message = String.format(
    			"Roles %s do not come from the set %s", 
    			invalidRoleSet, Roles.ALL_ROLES);
    		throw new IllegalArgumentException(message);
    	}
    	this.roles = roles;
    }
}
