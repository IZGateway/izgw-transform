package gov.cdc.izgateway.transformation.preconditions;

import gov.cdc.izgateway.transformation.annotations.CaptureXformAdvice;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.enums.DataType;
import gov.cdc.izgateway.transformation.logging.advice.Advisable;
import gov.cdc.izgateway.transformation.logging.advice.Transformable;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

import gov.cdc.izgateway.transformation.constants.XformConstants;

@Getter
@Setter
public class Equals implements Precondition, Advisable, Transformable {
    private UUID id;
    private String dataPath;
    private String comparisonValue;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Equals.class);

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
    @CaptureXformAdvice
    public boolean evaluate(ServiceContext context) {

        if (log.isTraceEnabled()) {
            log.trace("Precondition: {} / dataPath: '{}' / comparisonValue: '{}'",
                    this.getClass().getSimpleName(),
                    this.getDataPath(),
                    this.getComparisonValue());
        }

        if (this.dataPath.startsWith("state.")) {
            String stateKey = this.dataPath.split("\\.")[1];
            return Objects.equals(this.comparisonValue, context.getState().get(stateKey));
        } else if (getDataPath().equals(XformConstants.CONTEXT_FACILITY_ID_PATH)) {
            return context.getFacilityId().equals(getComparisonValue());
        } else if (context.getDataType().equals(DataType.HL7V2)) {
            return new Hl7v2Equals(this).evaluate(context);
        }

        return false;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public boolean hasTransformed() {
        // TODO - trying to see if this is what triggers getTransformedRequest and getTransformedResponse
        // TODO - run by Paul.  Should this only return true if the Precondition evaluations to true?  Not
        // sure I'm using this flag correctly.
        return true;
    }
}
