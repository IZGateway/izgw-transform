package gov.cdc.izgateway.xform.repository.file;

import gov.cdc.izgateway.common.HealthService;
import gov.cdc.izgateway.xform.model.*;
import gov.cdc.izgateway.xform.repository.RepositoryFactory;
import gov.cdc.izgateway.xform.repository.XformRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "spring.database", havingValue = "file", matchIfMissing = true)
public class FileRepositoryFactory implements RepositoryFactory {

    private final String accessControlFilePath;
    private final String groupRoleMappingFilePath;
    private final String mappingFilePath;
    private final String operationPreconditionFieldFilePath;
    private final String organizationFilePath;
    private final String pipelineFilePath;
    private final String solutionFilePath;
    private final String userFilePath;

    // Lazy-initialized repositories
    private AccessControlRepository acr;
    private GroupRoleMappingRepository grmr;
    private MappingRepository mr;
    private OperationPreconditionFieldRepository opfr;
    private OrganizationRepository or;
    private PipelineRepository pr;
    private SolutionRepository sr;
    private UserRepository ur;

    public FileRepositoryFactory(
            @Value("${xform.configurations.access-control}") String accessControlFilePath,
            @Value("${xform.configurations.group-role-mapping}") String groupRoleMappingFilePath,
            @Value("${xform.configurations.mappings}") String mappingFilePath,
            @Value("${xform.configurations.operation-precondition-fields}") String operationPreconditionFieldFilePath,
            @Value("${xform.configurations.organizations}") String organizationFilePath,
            @Value("${xform.configurations.pipelines}") String pipelineFilePath,
            @Value("${xform.configurations.solutions}") String solutionFilePath,
            @Value("${xform.configurations.users}") String userFilePath) {
        this.accessControlFilePath = accessControlFilePath;
        this.groupRoleMappingFilePath = groupRoleMappingFilePath;
        this.mappingFilePath = mappingFilePath;
        this.operationPreconditionFieldFilePath = operationPreconditionFieldFilePath;
        this.organizationFilePath = organizationFilePath;
        this.pipelineFilePath = pipelineFilePath;
        this.solutionFilePath = solutionFilePath;
        this.userFilePath = userFilePath;

        HealthService.setDatabase("files");
    }

    @Override
    public XformRepository<AccessControl> accessControlRepository() {
        if (acr == null) {
            acr = new AccessControlRepository();
            acr.setFilePath(accessControlFilePath);
        }
        return acr;
    }

    @Override
    public XformRepository<GroupRoleMapping> groupRoleMappingRepository() {
        if (grmr == null) {
            grmr = new GroupRoleMappingRepository();
            grmr.setFilePath(groupRoleMappingFilePath);
        }
        return grmr;
    }

    @Override
    public XformRepository<Mapping> mappingRepository() {
        if (mr == null) {
            mr = new MappingRepository();
            mr.setFilePath(mappingFilePath);
        }
        return mr;
    }

    @Override
    public XformRepository<OperationPreconditionField> operationPreconditionFieldRepository() {
        if (opfr == null) {
            opfr = new OperationPreconditionFieldRepository();
            opfr.setFilePath(operationPreconditionFieldFilePath);
        }
        return opfr;
    }

    @Override
    public XformRepository<Organization> organizationRepository() {
        if (or == null) {
            or = new OrganizationRepository();
            or.setFilePath(organizationFilePath);
        }
        return or;
    }

    @Override
    public XformRepository<Pipeline> pipelineRepository() {
        if (pr == null) {
            pr = new PipelineRepository();
            pr.setFilePath(pipelineFilePath);
        }
        return pr;
    }

    @Override
    public XformRepository<Solution> solutionRepository() {
        if (sr == null) {
            sr = new SolutionRepository();
            sr.setFilePath(solutionFilePath);
        }
        return sr;
    }

    @Override
    public XformRepository<User> userRepository() {
        if (ur == null) {
            ur = new UserRepository();
            ur.setFilePath(userFilePath);
        }
        return ur;
    }

    @Override
    public XformRepository<Event> eventRepository() {
        return null;
    }
}
