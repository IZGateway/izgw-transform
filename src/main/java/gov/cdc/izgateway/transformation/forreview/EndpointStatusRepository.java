package gov.cdc.izgateway.transformation.forreview;

import gov.cdc.izgateway.model.IDestination;
import gov.cdc.izgateway.model.IEndpointStatus;
import org.springframework.stereotype.Repository;

import java.util.List;

// TODO IGDD-1656: Do we need an EndpointStatusRepository in the transformation service?

@Repository
public class EndpointStatusRepository implements gov.cdc.izgateway.repository.EndpointStatusRepository {
    // TODO Paul - implement this the right way
    // Do we need an EndpointStatusRepository in the transformation service?
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
