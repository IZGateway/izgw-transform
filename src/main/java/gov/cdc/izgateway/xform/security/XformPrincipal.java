package gov.cdc.izgateway.xform.security;

import gov.cdc.izgateway.security.IzgPrincipal;
import java.util.Set;
import java.util.UUID;

/**
 * Principal that adds the organizations the user is allowed to access to an {@link IzgPrincipal}.
 */
public class XformPrincipal extends IzgPrincipal {
    private Set<UUID> allowedOrganizationIds;

    /**
     * Creates a principal that copies the identity, scopes and roles of another principal.
     *
     * @param principal the principal to copy
     */
    public XformPrincipal(IzgPrincipal principal) {
        this.setName(principal.getName());
        this.setOrganization(principal.getOrganization());
        this.setValidFrom(principal.getValidFrom());
        this.setValidTo(principal.getValidTo());
        this.setSerialNumber(principal.getSerialNumber());
        this.setIssuer(principal.getIssuer());
        this.setAudience(principal.getAudience());
        this.setScopes(principal.getScopes());
        this.setRoles(principal.getRoles());
    }

    @Override
    public String getSerialNumberHex() {
        return this.getSerialNumber();
    }

    /**
     * Sets the identifiers of the organizations the user is allowed to access.
     *
     * @param allowedOrganizationIds the allowed organization identifiers
     */
    public void setAllowedOrganizationIds(Set<UUID> allowedOrganizationIds) {
        this.allowedOrganizationIds = allowedOrganizationIds;
    }

    /**
     * Gets the identifiers of the organizations the user is allowed to access.
     *
     * @return the allowed organization identifiers
     */
    public Set<UUID> getAllowedOrganizationIds() {
        return allowedOrganizationIds;
    }
}
