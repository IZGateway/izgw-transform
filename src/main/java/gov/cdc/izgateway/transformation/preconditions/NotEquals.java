package gov.cdc.izgateway.transformation.preconditions;

import gov.cdc.izgateway.transformation.annotations.ExcludeField;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.enums.DataType;
import lombok.Getter;
import lombok.Setter;
import gov.cdc.izgateway.transformation.constants.XformConstants;

import java.util.Objects;

@Getter
@Setter
public class NotEquals extends Equals implements Precondition {

    @ExcludeField
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NotEquals.class);

    protected NotEquals() {
        super();
    }

    protected NotEquals(NotEquals notEquals) {
        super(notEquals);
    }

    @Override
    public boolean evaluate(ServiceContext context) {
        log.trace(String.format("Precondition: %s / dataPath: '%s' / comparisonValue: '%s'",
                this.getClass().getSimpleName(),
                this.getDataPath(),
                this.getComparisonValue()));

        if (this.getDataPath().startsWith("state.")) {
            String stateKey = this.getDataPath().split("\\.")[1];
            return !Objects.equals(this.getComparisonValue(), context.getState().get(stateKey));
        } else if (getDataPath().equals(XformConstants.CONTEXT_FACILITY_ID_PATH)) {
            return !context.getFacilityId().equals(getComparisonValue());
        } else if (context.getDataType().equals(DataType.HL7V2)) {
            return new Hl7v2NotEquals(this).evaluate(context);
        }

        return false;
    }
}
