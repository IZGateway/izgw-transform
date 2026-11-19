package gov.cdc.izgateway.xform.model;

import java.util.UUID;

/**
 * An object that belongs to an organization. Access to it is limited to users of that organization.
 */
public interface OrganizationAware {
    /**
     * Returns the identifier of the organization that the object belongs to.
     *
     * @return the organization identifier
     */
    UUID getOrganizationId();
}
