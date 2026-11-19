package gov.cdc.izgateway.xform.camel.converters;
import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.logging.event.TransactionData;
import gov.cdc.izgateway.soap.message.SubmitSingleMessageRequest;
import gov.cdc.izgateway.xform.context.IZGXformContext;
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

/**
 * Camel type converters between {@link IZGXformContext}, HL7 v2 message content, and files.
 */
@Component
@Slf4j
public class HubConverters implements TypeConverters {
    /**
     * Creates the converters.
     */
    public HubConverters() {
        // No initialization needed.
    }

    /**
     * Converts a context to a stream of its response HL7 v2 message.
     * @param context the transformation context
     * @return the response HL7 v2 message as an input stream
     */
    @Converter
    public InputStream contextToStream(IZGXformContext context) {
        String hl7Message = context.getSubmitSingleMessageResponse().getHl7Message();
        return new ByteArrayInputStream(hl7Message.getBytes());
    }

    /**
     * Converts a context to its response HL7 v2 message.
     * @param context the transformation context
     * @return the response HL7 v2 message
     */
    @Converter
    public String contextToString(IZGXformContext context) {
        return context.getSubmitSingleMessageResponse().getHl7Message();
    }

    /**
     * Creates a hub-to-hub HL7 v2 context from a file that contains an HL7 v2 message.
     * @param file the file that contains the HL7 v2 request message
     * @return a new context for the message
     */
    @Converter
    public IZGXformContext fileToContext(GenericFile<File> file) {
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

        return new IZGXformContext(serviceContext, request, null);

    }

}
