package gov.cdc.izgateway.transformation.preconditions;

import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.enums.DataType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Getter
@Setter
@Slf4j
public class Exists implements Precondition {

    private UUID id;
    private String dataPath;

    protected Exists() {}

    protected Exists(Exists exists) {
        this.id = exists.id;
        this.dataPath = exists.getDataPath();
    }

    protected Exists(String dataPath) {
        this.dataPath =  dataPath;
    }

    @Override
    public boolean evaluate(ServiceContext context) {
        log.trace(String.format("Precondition: %s / dataPath: '%s'",
                this.getClass().getSimpleName(),
                this.getDataPath()));

        if (context.getDataType().equals(DataType.HL7V2)) {
            return new Hl7v2Exists(this).evaluate(context);
        }

        return false;
    }

}
