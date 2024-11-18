package gov.cdc.izgateway.xform.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * This models information about an Operation, ultimate to let a user of the API know which field
 * are necessary when configuring a Operation in the system.
 */
@Getter
@Setter
public class OperationInfo {
    private String method;
    private Map<String, OperationInfoProperty> properties;
}
