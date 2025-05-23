package gov.cdc.izgateway.xform.migration;

import gov.cdc.izgateway.xform.model.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Configuration properties for entity migration.
 * Maps entity types to their configuration file paths.
 */
@Component
@ConfigurationProperties(prefix = "xform.configurations")
@Getter
@Setter
public class MigrationConfiguration {
    
    private String organizations;
    private String pipelines;
    private String solutions;
    private String mappings;
    private String accessControl;
    private String operationPreconditionFields;
    private String users;
    private String groupRoleMapping;
    
    /**
     * Gets a map of entity classes to their configuration file paths.
     * Add new entity types here as they get DynamoDB support.
     */
    public Map<Class<? extends BaseModel>, String> getEntityConfigurations() {
        return Map.of(
            Organization.class, organizations,
            AccessControl.class, accessControl,
            Pipeline.class, pipelines,
            GroupRoleMapping.class, groupRoleMapping,
            Mapping.class, mappings,
            OperationPreconditionField.class, operationPreconditionFields
            // Add more entities as they get DynamoDB repositories
            // Solution.class, solutions,
            // Mapping.class, mappings,
            // User.class, users,
            // GroupRoleMapping.class, groupRoleMapping,
            // OperationPreconditionField.class, operationPreconditionFields
        );
    }
}
