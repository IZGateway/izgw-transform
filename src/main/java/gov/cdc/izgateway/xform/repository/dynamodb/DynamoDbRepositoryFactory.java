package gov.cdc.izgateway.xform.repository.dynamodb;

import gov.cdc.izgateway.configuration.DynamoDbConfig;
import gov.cdc.izgateway.xform.model.*;
import gov.cdc.izgateway.xform.repository.RepositoryFactory;
import gov.cdc.izgateway.xform.repository.XformRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

@Component
@ConditionalOnExpression("'${spring.database:}'.equalsIgnoreCase('dynamodb') || '${spring.database:}'.equalsIgnoreCase('migrate') || '${spring.database:}'.equalsIgnoreCase('reinit')")
public class DynamoDbRepositoryFactory implements RepositoryFactory {

    private final DynamoDbEnhancedClient dynamoDbClient;
    private final String tableName;

    // Lazy-initialized repositories
    private AccessControlRepository acr;
    private GroupRoleMappingRepository grmr;
    private MappingRepository mr;
    private OperationPreconditionFieldRepository opfr;
    private OrganizationRepository or;
    private PipelineRepository pr;
    private SolutionRepository sr;
    private UserRepository ur;
    private EventRepository er;

    public DynamoDbRepositoryFactory(
            DynamoDbEnhancedClient dynamoDbClient,
            DynamoDbConfig dynamoDbConfig) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = dynamoDbConfig.getDynamodbTable();
    }

    @Override
    public XformRepository<AccessControl> accessControlRepository() {
        if (acr == null) {
            acr = new AccessControlRepository(dynamoDbClient, tableName);
        }
        return acr;
    }

    @Override
    public XformRepository<GroupRoleMapping> groupRoleMappingRepository() {
        if (grmr == null) {
            grmr = new GroupRoleMappingRepository(dynamoDbClient, tableName);
        }
        return grmr;
    }

    @Override
    public XformRepository<Mapping> mappingRepository() {
        if (mr == null) {
            mr = new MappingRepository(dynamoDbClient, tableName);
        }
        return mr;
    }

    @Override
    public XformRepository<OperationPreconditionField> operationPreconditionFieldRepository() {
        if (opfr == null) {
            opfr = new OperationPreconditionFieldRepository(dynamoDbClient, tableName);
        }
        return opfr;
    }

    @Override
    public XformRepository<Organization> organizationRepository() {
        if (or == null) {
            or = new OrganizationRepository(dynamoDbClient, tableName);
        }
        return or;
    }

    @Override
    public XformRepository<Pipeline> pipelineRepository() {
        if (pr == null) {
            pr = new PipelineRepository(dynamoDbClient, tableName);
        }
        return pr;
    }

    @Override
    public XformRepository<Solution> solutionRepository() {
        if (sr == null) {
            sr = new SolutionRepository(dynamoDbClient, tableName);
        }
        return sr;
    }

    @Override
    public XformRepository<User> userRepository() {
        if (ur == null) {
            ur = new UserRepository(dynamoDbClient, tableName);
        }
        return ur;
    }

    @Override
    public XformRepository<Event> eventRepository() {
        if (er == null) {
            er = new EventRepository(dynamoDbClient, tableName);
        }
        return er;
    }
}
