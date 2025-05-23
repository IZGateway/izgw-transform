package gov.cdc.izgateway.xform.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.util.UUID;

@Getter
@Setter
@DynamoDbBean
public class OperationPreconditionField extends BaseModel {

    private UUID id;

    @NotBlank(message = "Field name is required")
    private String fieldName;

    @NotNull(message = "Field active status is required")
    private String dataPath;

    @NotNull(message = "Field is for Precondition is required")
    private Boolean forPrecondition;

    @NotNull(message = "Field is for Operation is required")
    private Boolean forOperation;

    @NotNull(message = "Field active status is required")
    private Boolean active;
}
