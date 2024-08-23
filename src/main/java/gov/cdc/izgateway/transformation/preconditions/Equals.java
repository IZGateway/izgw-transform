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
public class Equals implements Precondition {
    private UUID id;
    private String dataPath;
    private String comparisonValue;

    protected Equals() {

    }

    protected Equals(Equals equals) {
        this.id = equals.id;
        this.dataPath = equals.getDataPath();
        this.comparisonValue = equals.getComparisonValue();
    }

    protected Equals(String dataPath, String comparisonValue) {
        this.dataPath = dataPath;
        this.comparisonValue = comparisonValue;
    }

    @Override
    public boolean evaluate(ServiceContext context) {

        log.trace(String.format("Precondition: %s / dataPath: '%s' / comparisonValue: '%s'",
                this.getClass().getSimpleName(),
                this.getDataPath(),
                this.getComparisonValue()));

        if (getDataPath().equals("context.FacilityID")) {
            return context.getFacilityId().equals(getComparisonValue());
        } else if (context.getDataType().equals(DataType.HL7V2)) {
            return new Hl7v2Equals(this).evaluate(context);
        }

        return false;
    }
}
