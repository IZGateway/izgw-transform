package gov.cdc.izgateway.transformation.security;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class Roles {
    // TODO: Discuss with the team how to manage the roles between xform and izgateway core
    public static final String ADMIN = "admin";
    public static final String XFORM_SENDING_SYSTEM = "xform-sending-system";
    public static final String XFORM_CONSOLE_USER = "xform-console-user";

    public static final String INTERNAL = "internal";
    public static final String OPERATIONS = "operations";
    public static final String BLACKLIST = "blacklist";
    public static final String SOAP = "soap";
    public static final String USERS = "users";
    public static final String OPEN = "OPEN";

    private static final Set<String> SUPPORTED_ROLES = new HashSet<>();

    static {
        for (Field field : Roles.class.getDeclaredFields()) {
            if (field.getType().equals(String.class)) {
                try {
                    SUPPORTED_ROLES.add((String) field.get(null));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to initialize supported roles", e);
                }
            }
        }
    }

    public static boolean isSupportedRole(String role) {
        return SUPPORTED_ROLES.contains(role);
    }

}
