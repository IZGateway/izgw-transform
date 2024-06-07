package gov.cdc.izgateway.transformation.model;

import gov.cdc.izgateway.transformation.configuration.OrganizationConfig;
import gov.cdc.izgateway.transformation.repository.OrganizationRepository;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
public class Organization {
    private String organizationName;
    private UUID organizationId;
    private Boolean active;

}
