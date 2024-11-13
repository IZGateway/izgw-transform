package gov.cdc.izgateway.transformation.preconditions;

import gov.cdc.izgateway.transformation.annotations.CaptureXformAdvice;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.enums.DataType;
import gov.cdc.izgateway.transformation.logging.advice.Advisable;
import gov.cdc.izgateway.transformation.logging.advice.Transformable;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Exists implements Precondition, Advisable, Transformable {

    private UUID id;
    private String dataPath;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Exists.class);

    protected Exists() {
    }

    protected Exists(Exists exists) {
        this.id = exists.id;
        this.dataPath = exists.getDataPath();
    }

    protected Exists(String dataPath) {
        this.dataPath = dataPath;
    }

    @Override
    @CaptureXformAdvice
    public boolean evaluate(ServiceContext context) {
        if (log.isTraceEnabled()) {
            log.trace("Precondition: {} / dataPath: '{}'",
                    this.getClass().getSimpleName(),
                    this.getDataPath());
        }

        if (this.dataPath.startsWith("state.")) {
            String stateKey = this.dataPath.split("\\.")[1];
            return context.getState().containsKey(stateKey);
        } else if (context.getDataType().equals(DataType.HL7V2)) {
            return new Hl7v2Exists(this).evaluate(context);
        }

        return false;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public boolean hasTransformed() {
        return true;
    }
}
