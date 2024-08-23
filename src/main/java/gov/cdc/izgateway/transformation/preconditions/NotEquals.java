package gov.cdc.izgateway.transformation.preconditions;

import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.enums.DataType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import gov.cdc.izgateway.transformation.constants.XformConstants;

@Getter
@Setter
@Slf4j
public class NotEquals extends Equals implements Precondition {

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

        if (getDataPath().equals(XformConstants.CONTEXT_FACILITY_ID_PATH)) {
            return !context.getFacilityId().equals(getComparisonValue());
        } else if (context.getDataType().equals(DataType.HL7V2)) {
            return new Hl7v2NotEquals(this).evaluate(context);
        }

        return false;
    }
}
