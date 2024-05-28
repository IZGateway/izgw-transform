package gov.cdc.izgateway.transformation.pipelines;

import gov.cdc.izgateway.transformation.configuration.ServiceConfig;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log
@Component
public class DataPipeline {
    private ServiceConfig serviceConfig;
    private ServiceContext serviceContext;

    @Autowired
    public DataPipeline(ServiceConfig serviceConfig, ServiceContext serviceContext) {
        this.serviceConfig = serviceConfig;
        this.serviceContext = serviceContext;
    }

}
