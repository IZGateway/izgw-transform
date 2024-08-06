package gov.cdc.izgateway.transformation.services;

import gov.cdc.izgateway.model.IMessageHeader;
import gov.cdc.izgateway.service.IMessageHeaderService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * A ticket has been added to address this class in the future.  It is not needed for Transformation Servivce,
 * but it is required as of now because the IZG Core library requires it.
 * This class contains no-ops for all methods.
 * Ticket to track this: https://support.izgateway.org/browse/IGDD-1663
 */
@Service
public class MessageHeaderService implements IMessageHeaderService {
    @Override
    public void refresh() {
        System.out.println("MessageHeaderService.refresh");
    }

    @Override
    public IMessageHeader findByMsgId(String msgId) {
        return null;
    }

    @Override
    public List<IMessageHeader> getMessageHeaders(List<String> mshList) {
        return List.of();
    }

    @Override
    public List<IMessageHeader> getAllMessageHeaders() {
        return List.of();
    }

    @Override
    public String getSourceType(String... idList) {
        return "";
    }

    @Override
    public IMessageHeader saveAndFlush(IMessageHeader h) {
        return null;
    }
}
