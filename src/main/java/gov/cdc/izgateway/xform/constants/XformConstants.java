package gov.cdc.izgateway.xform.constants;

public final class XformConstants {

    private XformConstants() {
        throw new UnsupportedOperationException("Constants class and cannot be instantiated");
    }

    public static final String CONTEXT_FACILITY_ID_PATH = "context.FacilityID";

    /**
     * Reserved URL path prefix for SQL backend endpoints served by izgw-transform-sql.
     * IIS destination IDs that would conflict with this namespace are rejected at registration.
     */
    public static final String SQL_PATH_PREFIX = "/sql/";
}
