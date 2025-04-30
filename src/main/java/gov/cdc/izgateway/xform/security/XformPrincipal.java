package gov.cdc.izgateway.xform.security;

import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.security.IzgPrincipal;
import gov.cdc.izgateway.xform.model.User;

import java.util.Set;
import java.util.UUID;

public class XformPrincipal extends IzgPrincipal {
    private Set<UUID> allowedOrganizationIds;

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

    public void setAllowedOrganizationIds(Set<UUID> allowedOrganizationIds) {
        this.allowedOrganizationIds = allowedOrganizationIds;
    }

    public Set<UUID> getAllowedOrganizationIds() {
        return allowedOrganizationIds;
    }
}
