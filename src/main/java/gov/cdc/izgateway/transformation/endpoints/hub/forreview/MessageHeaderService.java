package gov.cdc.izgateway.transformation.endpoints.hub.forreview;

import gov.cdc.izgateway.model.IMessageHeader;
import gov.cdc.izgateway.service.IMessageHeaderService;
import org.springframework.stereotype.Service;

import java.util.List;

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
