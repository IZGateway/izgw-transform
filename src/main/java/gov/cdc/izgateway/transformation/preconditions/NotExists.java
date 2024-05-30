package gov.cdc.izgateway.transformation.preconditions;

import ca.uhn.hl7v2.model.Message;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotExists extends Exists implements Precondition {
    protected NotExists() {}

    protected NotExists(NotExists notExists) {
        super(notExists);
    }

    protected NotExists(String dataPath) {
        super(dataPath);
    }

    @Override
    public boolean evaluate(Message message) {
        return false;
    }
}
