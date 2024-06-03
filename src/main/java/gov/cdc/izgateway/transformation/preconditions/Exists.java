package gov.cdc.izgateway.transformation.preconditions;

import ca.uhn.hl7v2.model.Message;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Exists implements Precondition {

    private String dataPath;

    protected Exists() {}

    protected Exists(Exists exists) {
        this.dataPath = exists.getDataPath();
    }

    protected Exists(String dataPath) {
        this.dataPath =  dataPath;
    }

    @Override
    public boolean evaluate(Message message) {
        return false;
    }

}
