package gov.cdc.izgateway.xform.preconditions;

import gov.cdc.izgateway.xform.annotations.CaptureXformAdvice;
import gov.cdc.izgateway.xform.context.ServiceContext;
import gov.cdc.izgateway.xform.enums.DataType;
import lombok.Getter;
import lombok.Setter;

/**
 * Precondition that passes when the data path has no value.
 */
@Getter
@Setter
public class NotExists extends Exists implements Precondition {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NotExists.class);

    /**
     * Creates an empty precondition.
     */
    protected NotExists() {
    }

    /**
     * Creates a precondition with the settings of another precondition.
     * @param notExists the precondition to copy the settings from
     */
    protected NotExists(NotExists notExists) {
        super(notExists);
    }

    @Override
    @CaptureXformAdvice
    public boolean evaluate(ServiceContext context) {
        if (log.isTraceEnabled()) {
            log.trace("Precondition: {} / dataPath: '{}'",
                    this.getClass().getSimpleName(),
                    this.getDataPath());
        }

        if (this.getDataPath().startsWith("state.")) {
            String stateKey = this.getDataPath().split("\\.")[1];
            return !context.getState().containsKey(stateKey);
        } else if (context.getDataType().equals(DataType.HL7V2)) {
            return new Hl7v2NotExists(this).evaluate(context);
        }

        return false;
    }
}
