package gov.cdc.izgateway.transformation.model;

import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
public class Organization {
    private String organizationName;
    private UUID organizationId;
    private Boolean active;

}
