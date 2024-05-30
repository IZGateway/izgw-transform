package gov.cdc.izgateway.transformation.preconditions;

import ca.uhn.hl7v2.model.Message;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Hl7v2NotExists extends NotExists implements Precondition {
    public Hl7v2NotExists() {
        super();
    }

    public Hl7v2NotExists(NotExists notExists) {
        super(notExists);
    }

    public Hl7v2NotExists(String dataPath) {
        super(dataPath);
    }

    @Override
    public boolean evaluate(Message message) {

        log.trace(String.format("Precondition: %s / dataPath: '%s'",
                this.getClass().getSimpleName(),
                this.getDataPath()));

        Hl7v2Exists exists = new Hl7v2Exists(this.getDataPath());
        return !exists.evaluate(message);
    }
}
