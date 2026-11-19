package gov.cdc.izgateway.xform.constants;

/**
 * Constants for the transformation service.
 */
public final class XformConstants {

    private XformConstants() {
        throw new UnsupportedOperationException("Constants class and cannot be instantiated");
    }

    /**
     * Precondition data path that selects the facility ID of the service context.
     */
    public static final String CONTEXT_FACILITY_ID_PATH = "context.FacilityID";
}
