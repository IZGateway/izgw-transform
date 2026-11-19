package gov.cdc.izgateway.xform.preconditions;

import gov.cdc.izgateway.xform.annotations.CaptureXformAdvice;
import gov.cdc.izgateway.xform.context.ServiceContext;
import gov.cdc.izgateway.xform.enums.DataType;
import gov.cdc.izgateway.xform.logging.advice.Advisable;
import gov.cdc.izgateway.xform.logging.advice.Transformable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.UUID;

import gov.cdc.izgateway.xform.constants.XformConstants;


/**
 * A precondition that is true if the value at a data path is equal to a comparison value.
 */
@Getter
@Setter
@Slf4j
public class Equals implements Precondition, Advisable, Transformable {
    @NotNull(message = "required and cannot be empty")
    private UUID id;
    @NotNull(message = "required and cannot be empty")
    private String dataPath;
    @NotNull(message = "required and cannot be empty")
    private String comparisonValue;

    /**
     * Creates an empty precondition.
     */
    protected Equals() {

    }

    /**
     * Creates a precondition with the same id, data path and comparison value as another one.
     *
     * @param equals the precondition to copy
     */
    protected Equals(Equals equals) {
        this.id = equals.id;
        this.dataPath = equals.getDataPath();
        this.comparisonValue = equals.getComparisonValue();
    }

    /**
     * Creates a precondition for a data path and a comparison value.
     *
     * @param dataPath the path of the value to compare
     * @param comparisonValue the value to compare with
     */
    protected Equals(String dataPath, String comparisonValue) {
        this.dataPath = dataPath;
        this.comparisonValue = comparisonValue;
    }

    @Override
    @CaptureXformAdvice
    public boolean evaluate(ServiceContext context) {

        if (log.isDebugEnabled()) {
            log.debug("Precondition: {} / dataPath: '{}' / comparisonValue: '{}'",
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
        return true;
    }
}
