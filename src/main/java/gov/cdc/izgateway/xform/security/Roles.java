package gov.cdc.izgateway.xform.security;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

    /** Header to indicate that request from localhost should not be treated as an admin */
    public static final String NOT_ADMIN_HEADER = "X-Not-Admin";

    /** Header to indicate that the request should remove admin role from the principal to test non-admin access.
     *  This is different from NOT_ADMIN_HEADER in that it is used to remove the admin role regardless of whether
     *  the request is from localhost or not.
     */
    public static final String REMOVE_ADMIN_ROLE_HEADER = "X-Remove-Admin-Role";

    // Special role for public access to an API endpoint.  Health check endpoints use this role.
    public static final String PUBLIC_ACCESS = "public-access";

    /**
     * A list of all roles to enable validation of role names
     * in model elements.
     */
    public static final List<String> ALL_ROLES = Collections.unmodifiableList(
    	Arrays.asList(
    		ADMIN, XFORM_SENDING_SYSTEM, 
    		PIPELINE_READER, PIPELINE_WRITER, PIPELINE_DELETER,
    		ORGANIZATION_READER, ORGANIZATION_WRITER, ORGANIZATION_DELETER,
    		SOLUTION_READER, SOLUTION_WRITER, SOLUTION_DELETER, PUBLIC_ACCESS
    	)
    );
    private Roles() {}
}
