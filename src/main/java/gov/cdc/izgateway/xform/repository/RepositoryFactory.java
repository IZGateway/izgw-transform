package gov.cdc.izgateway.xform.repository;

import gov.cdc.izgateway.xform.model.*;

public interface RepositoryFactory {
    XformRepository<AccessControl> accessControlRepository();
    XformRepository<GroupRoleMapping> groupRoleMappingRepository();
    XformRepository<Mapping> mappingRepository();
    XformRepository<OperationPreconditionField> operationPreconditionFieldRepository();
    XformRepository<Organization> organizationRepository();
    XformRepository<Pipeline> pipelineRepository();
    XformRepository<Solution> solutionRepository();
    XformRepository<User> userRepository();
    XformRepository<Event> eventRepository();
}
