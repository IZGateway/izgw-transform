package gov.cdc.izgateway.xform.repository;

import gov.cdc.izgateway.xform.model.AccessControl;
import gov.cdc.izgateway.xform.model.GroupRoleMapping;
import gov.cdc.izgateway.xform.model.Mapping;
import gov.cdc.izgateway.xform.model.OperationPreconditionField;
import gov.cdc.izgateway.xform.model.Organization;
import gov.cdc.izgateway.xform.model.Pipeline;
import gov.cdc.izgateway.xform.model.Solution;
import gov.cdc.izgateway.xform.model.User;

public interface RepositoryFactory {
    XformRepository<AccessControl> accessControlRepository();
    XformRepository<GroupRoleMapping> groupRoleMappingRepository();
    XformRepository<Mapping> mappingRepository();
    XformRepository<OperationPreconditionField> operationPreconditionFieldRepository();
    XformRepository<Organization> organizationRepository();
    XformRepository<Pipeline> pipelineRepository();
    XformRepository<Solution> solutionRepository();
    XformRepository<User> userRepository();
}
