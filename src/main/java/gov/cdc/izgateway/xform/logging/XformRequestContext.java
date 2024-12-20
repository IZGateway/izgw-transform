package gov.cdc.izgateway.xform.logging;

public class XformRequestContext {
    private XformRequestContext() {}
    private static ThreadLocal<Boolean> disableApiLogging = ThreadLocal.withInitial(() -> Boolean.FALSE);

    /**
     * API logging is only enabled for calls to our API endpoints. This is to prevent logging for internal calls like
     * those made by the transformation service itself when processing messages.  We will capture logging for the API
     * calls to log/audit the changes made to the data by end users of the Xform Console, or other systems that call our
     * API.
     * @return true if API logging is disabled, false otherwise
     */
    public static boolean isApiLoggingDisabled() {
        return disableApiLogging.get();
    }

    public static void disableApiLogging() {
        disableApiLogging.set(Boolean.TRUE);
    }

    public static void clear() {
        disableApiLogging.remove();
    }
}
