package gov.cdc.izgateway.transformation.preconditions;

import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.enums.DataType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class NotExists extends Exists implements Precondition {
    protected NotExists(NotExists notExists) {
        super(notExists);
    }

    protected NotExists(String dataPath) {
        super(dataPath);
    }

    @Override
    public boolean evaluate(ServiceContext context) {
        log.trace(String.format("Precondition: %s / dataPath: '%s'",
                this.getClass().getSimpleName(),
                this.getDataPath()));

        if (context.getDataType().equals(DataType.HL7V2)) {
            return new Hl7v2NotExists(this).evaluate(context);
        }

        return false;
    }
}
