package gov.cdc.izgateway.transformation.preconditions;

import gov.cdc.izgateway.transformation.context.ServiceContext;
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
    public boolean evaluate(ServiceContext context) {
        return false;
    }
}
