package gov.cdc.izgateway.xform.repository;

import gov.cdc.izgateway.xform.model.*;

/**
 * Gives the repository for each entity type of the selected storage (file or DynamoDB).
 */
public interface RepositoryFactory {
    /**
     * Returns the repository for access control entities.
     *
     * @return the repository
     */
    XformRepository<AccessControl> accessControlRepository();
    /**
     * Returns the repository for group role mapping entities.
     *
     * @return the repository
     */
    XformRepository<GroupRoleMapping> groupRoleMappingRepository();
    /**
     * Returns the repository for mapping entities.
     *
     * @return the repository
     */
    XformRepository<Mapping> mappingRepository();
    /**
     * Returns the repository for operation precondition field entities.
     *
     * @return the repository
     */
    XformRepository<OperationPreconditionField> operationPreconditionFieldRepository();
    /**
     * Returns the repository for organization entities.
     *
     * @return the repository
     */
    XformRepository<Organization> organizationRepository();
    /**
     * Returns the repository for pipeline entities.
     *
     * @return the repository
     */
    XformRepository<Pipeline> pipelineRepository();
    /**
     * Returns the repository for solution entities.
     *
     * @return the repository
     */
    XformRepository<Solution> solutionRepository();
    /**
     * Returns the repository for user entities.
     *
     * @return the repository
     */
    XformRepository<User> userRepository();
    /**
     * Returns the repository for event entities.
     *
     * @return the repository
     */
    XformRepository<Event> eventRepository();
}
