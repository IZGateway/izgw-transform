package gov.cdc.izgateway.transformation.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * This models information about a Precondition, ultimate to let a user of the API know which field
 * are necessary when configuring a Precondition in the system.
 */
@Getter
@Setter
public class PreconditionInfo {
    private String method;
    private Map<String, PreconditionInfoProperty> properties;
}
