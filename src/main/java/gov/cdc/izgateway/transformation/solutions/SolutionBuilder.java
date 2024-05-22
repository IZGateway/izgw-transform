package gov.cdc.izgateway.transformation.solutions;

import gov.cdc.izgateway.transformation.configuration.ServiceConfig;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log
public class SolutionBuilder {
    private final ServiceConfig serviceConfig;

    @Autowired
    public SolutionBuilder(ServiceConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
    }

    public Solution build(ServiceContext context) {
        return null;
    }
}
