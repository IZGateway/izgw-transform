package gov.cdc.izgateway.transformation.preconditions;

import ca.uhn.hl7v2.model.Message;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Equals implements Precondition {
    private String dataPath;
    private String comparisonValue;

    protected Equals() {

    }

    protected Equals(Equals equals) {
        this.dataPath = equals.getDataPath();
        this.comparisonValue = equals.getComparisonValue();
    }

    protected Equals(String dataPath, String comparisonValue) {
        this.dataPath = dataPath;
        this.comparisonValue = comparisonValue;
    }

    @Override
    public boolean evaluate(Message message) {
        return false;
    }
}
