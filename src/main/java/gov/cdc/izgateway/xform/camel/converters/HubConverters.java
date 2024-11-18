package gov.cdc.izgateway.xform.camel.converters;
import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.logging.event.TransactionData;
import gov.cdc.izgateway.soap.message.SubmitSingleMessageRequest;
import gov.cdc.izgateway.xform.context.IZGTransformationContext;
import gov.cdc.izgateway.xform.context.ServiceContext;
import gov.cdc.izgateway.xform.enums.DataType;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Converter;
import org.apache.camel.TypeConverters;
import org.apache.camel.component.file.GenericFile;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.UUID;

@Component
@Slf4j
public class HubConverters implements TypeConverters {
    @Converter
    public InputStream contextToStream(IZGTransformationContext context) {
        String hl7Message = context.getSubmitSingleMessageResponse().getHl7Message();
        return new ByteArrayInputStream(hl7Message.getBytes());
    }

    @Converter
    public String contextToString(IZGTransformationContext context) {
        return context.getSubmitSingleMessageResponse().getHl7Message();
    }

    @Converter
    public IZGTransformationContext fileToContext(GenericFile<File> file) {
        File thebody = (File) file.getBody();

        UUID organization = UUID.fromString("0d15449b-fb08-4013-8985-20c148b353fe");
        ServiceContext serviceContext = null;
        String body;
        try {
            body = new String(Files.readAllBytes(thebody.toPath()));
            serviceContext = new ServiceContext(organization,
                    "izgts:IISHubService",
                    "izghub:IISHubService",
                    DataType.HL7V2,
                    "", // This isn't used so blanking FacilityID for now
                    body);
        } catch (Exception e) {
            log.error("Error creating ServiceContext: " + e.getMessage());
            throw new RuntimeException("Error creating ServiceContext: " + e.getMessage());
        }

        SubmitSingleMessageRequest request = new SubmitSingleMessageRequest();
        request.getHubHeader().setDestinationId("dev");

        // TODO this needs to be addressed
        TransactionData t = new TransactionData("TODO: A Real EVENTID");
        RequestContext.setTransactionData(t);

        return new IZGTransformationContext(serviceContext, request, null);

    }

}
