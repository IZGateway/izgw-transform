package gov.cdc.izgateway.transformation.forreview;

import gov.cdc.izgateway.model.IMessageHeader;
import gov.cdc.izgateway.service.IMessageHeaderService;
import org.springframework.stereotype.Service;

import java.util.List;

// TODO: IGDD-1656: Do we need a MessageHeaderService in the transformation service?  It uses a DB table.
// There are some fields for username, password, etc that we may not need.
@Service
public class MessageHeaderService implements IMessageHeaderService {
    @Override
    public void refresh() {

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
