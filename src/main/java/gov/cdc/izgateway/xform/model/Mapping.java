package gov.cdc.izgateway.xform.model;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.util.UUID;

@Getter
@Setter
@DynamoDbBean
public class Mapping extends BaseModel implements OrganizationAware {
    private UUID id;
    @NotNull(message = "Mapping active status is required")
    private Boolean active;
    @NotNull(message = "Organization ID is required")
    private UUID organizationId;
    @NotNull(message = "Code system is required")
    private String codeSystem;
    @NotNull(message = "Code is required")
    private String code;
    @NotNull(message = "Target code system is required")
    private String targetCodeSystem;
    @NotNull(message = "Target code is required")
    private String targetCode;
}
