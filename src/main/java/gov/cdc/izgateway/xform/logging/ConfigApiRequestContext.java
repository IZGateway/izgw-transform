//package gov.cdc.izgateway.xform.logging;
//
//import gov.cdc.izgateway.logging.RequestContext;
//import gov.cdc.izgateway.logging.event.TransactionData;
//import gov.cdc.izgateway.logging.info.DestinationInfo;
//import gov.cdc.izgateway.logging.info.SourceInfo;
//import gov.cdc.izgateway.security.IzgPrincipal;
//import jakarta.servlet.http.HttpServletResponse;
//
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
///**
// * RequestContext is a class intended to provide access to variables relevant to the
// * current request that we don't want to store in the MDC as the MDC variables get
// * logged.
// */
//public class ConfigApiRequestContext {
//	private ConfigApiRequestContext() {}
//
//    // TODO: Paul - have this class use regular RequestContext for things like principal.  Add variables
//    // for logging data only.
//	private static ThreadLocal<XformApiLogDetail> apiLogDetailVar = new ThreadLocal<>();
//	public static XformApiLogDetail getApiLogDetail() {
//		return apiLogDetailVar.get();
//	}
//
//	public static void setApiLogDetail(XformApiLogDetail apiLogDetail) {
//		if (apiLogDetail == null) {
//			clear();
//			return;
//		}
//        apiLogDetailVar.set(apiLogDetail);
//	}
//
//	public static void clear() {
//		apiLogDetailVar.remove();
//	}
////	public static TransactionData init() {
////		TransactionData tData = new TransactionData();
////		RequestContext.setTransactionData(tData);
////		return tData;
////	}
//}
