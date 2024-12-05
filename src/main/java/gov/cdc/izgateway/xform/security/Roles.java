package gov.cdc.izgateway.xform.security;

public class Roles {
    // Supported roles
    public static final String ADMIN = "admin";
    public static final String XFORM_SENDING_SYSTEM = "xform-sender";

    public static final String PIPELINE_READER = "pipeline-reader";
    public static final String PIPELINE_WRITER = "pipeline-writer";
    public static final String PIPELINE_DELETER = "pipeline-deleter";

    public static final String ORGANIZATION_READER = "organization-reader";
    public static final String ORGANIZATION_WRITER = "organization-writer";
    public static final String ORGANIZATION_DELETER = "organization-deleter";

    public static final String SOLUTION_READER = "solution-reader";
    public static final String SOLUTION_WRITER = "solution-writer";
    public static final String SOLUTION_DELETER = "solution-deleter";

    // Header to indicate that request from localhost should not be treated as an admin
    public static final String NOT_ADMIN_HEADER = "X-Not-Admin";

    private Roles() {}
}
