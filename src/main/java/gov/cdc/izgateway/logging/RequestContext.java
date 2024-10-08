package gov.cdc.izgateway.logging;

import gov.cdc.izgateway.logging.event.TransactionData;
import gov.cdc.izgateway.logging.info.DestinationInfo;
import gov.cdc.izgateway.logging.info.SourceInfo;
import gov.cdc.izgateway.transformation.model.User;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * RequestContext is a class intended to provide access to variables relevant to the
 * current request that we don't want to store in the MDC as the MDC variables get
 * logged.
 */
public class RequestContext {
    private RequestContext() {}
    private static ThreadLocal<TransactionData> transactionDataVar = new ThreadLocal<>();
    private static ThreadLocal<SourceInfo> sourceInfoVar = new ThreadLocal<>();
    private static ThreadLocal<DestinationInfo> destinationInfoVar = new ThreadLocal<>();
    private static ThreadLocal<String> eventIdVar = new ThreadLocal<>();
    private static ThreadLocal<Boolean> disableLogging = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private static ThreadLocal<Set<String>> roles = ThreadLocal.withInitial(() -> new HashSet<String>());
    private static ThreadLocal<User> userVar = new ThreadLocal<>();
    private static ThreadLocal<HttpServletResponse> responseVar = new ThreadLocal<>();
    private static ThreadLocal<Map<String, List<String>>> httpHeadersVar = new ThreadLocal<>();
    public static TransactionData getTransactionData() {
        return transactionDataVar.get();
    }
    public static void setTransactionData(TransactionData transactionData) {
        if (transactionData == null) {
            clear();
            return;
        }
        transactionDataVar.set(transactionData);
        sourceInfoVar.set(transactionData.getSource());
        destinationInfoVar.set(transactionData.getDestination());
        eventIdVar.set(transactionData.getEventId());
    }
    public static Set<String> initRoles() {
        Set<String> init = new HashSet<>();
        roles.set(init);
        return init;
    }

    public static SourceInfo getSourceInfo() {
        return sourceInfoVar.get();
    }
    public static DestinationInfo getDestinationInfo() {
        return destinationInfoVar.get();
    }
    public static String getEventId() {
        return eventIdVar.get();
    }

    public static Set<String> getRoles() {
        return roles.get();
    }

    public static void clear() {
        transactionDataVar.remove();
        sourceInfoVar.remove();
        destinationInfoVar.remove();
        eventIdVar.remove();
        disableLogging.remove();
        roles.remove();
        responseVar.remove();
        httpHeadersVar.remove();
        userVar.remove();
    }
    public static void disableTransactionDataLogging() {
        disableLogging.set(Boolean.TRUE);
    }
    public static boolean isLoggingDisabled() {
        return disableLogging.get();
    }
    public static TransactionData init() {
        TransactionData tData = new TransactionData();
        RequestContext.setTransactionData(tData);
        return tData;
    }
    public static void setResponse(HttpServletResponse resp) {
        responseVar.set(resp);
    }
    public static HttpServletResponse getResponse() {
        return responseVar.get();
    }

    public static void setHttpHeaders(Map<String, List<String>> headers) {
        httpHeadersVar.set(headers);
    }

    public static Map<String, List<String>> getHttpHeaders() {
        return httpHeadersVar.get();
    }

    public static void setUser(User user) {
        userVar.set(user);
    }

    public static User getUser() {
        return userVar.get();
    }
}
