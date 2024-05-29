package gov.cdc.izgateway.transformation.preconditions;

import ca.uhn.hl7v2.model.Message;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotEquals extends Equals implements Precondition {

    protected NotEquals() {
        super();
    }

    protected NotEquals(NotEquals notEquals) {
        super(notEquals);
    }

    protected NotEquals(String dataPath, String comparisonValue) {
        super(dataPath, comparisonValue);
    }

    @Override
    public boolean evaluate(Message message) {
        return false;
    }
}
