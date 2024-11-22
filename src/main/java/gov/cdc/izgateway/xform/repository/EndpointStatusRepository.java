package gov.cdc.izgateway.xform.repository;

import gov.cdc.izgateway.model.IDestination;
import gov.cdc.izgateway.model.IEndpointStatus;
import gov.cdc.izgateway.xform.model.EndpointStatus;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * A ticket has been added to address this class in the future.  It is not needed for Transformation Servivce.
 * This class contains no-ops for all methods.
 * Ticket to track this: https://support.izgateway.org/browse/IGDD-1665
 */
@Repository
public class EndpointStatusRepository implements gov.cdc.izgateway.repository.EndpointStatusRepository {
    private IEndpointStatus status = new EndpointStatus();

    @Override
    public List<IEndpointStatus> findAll() {
        return List.of();
    }

    @Override
    public IEndpointStatus findById(String id) {
        return status;
    }

    @Override
    public IEndpointStatus saveAndFlush(IEndpointStatus status) {
        return status;
    }

    @Override
    public boolean removeById(String id) {
        return false;
    }

    @Override
    public List<IEndpointStatus> find(int maxQuarterHours, String[] include) {
        return List.of();
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public IEndpointStatus newEndpointStatus() {
        return status;
    }

    @Override
    public IEndpointStatus newEndpointStatus(IDestination dest) {
        return status;
    }
}
