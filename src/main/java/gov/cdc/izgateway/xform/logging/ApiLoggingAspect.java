//package gov.cdc.izgateway.xform.logging;
//
//import gov.cdc.izgateway.logging.RequestContext;
//import gov.cdc.izgateway.logging.markers.Markers2;
//import gov.cdc.izgateway.security.IzgPrincipal;
//import gov.cdc.izgateway.xform.model.Pipeline;
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.annotation.AfterReturning;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Aspect
//@Component
//public class ApiLoggingAspect {
//
//    private final HttpServletRequest request;
//
//    @Autowired
//    public ApiLoggingAspect(HttpServletRequest request) {
//        this.request = request;
//    }
//
//    // AfterReturning
//    @Before("execution(* gov.cdc.izgateway.xform.ApiController.*(..)) || " +
//                    "execution(* gov.cdc.izgateway.xform.XformApplicationController.*(..)) || " +
//                    "execution(* gov.cdc.izgateway.xform.LogController.*(..)) || " +
//                    "execution(* gov.cdc.izgateway.xform.endpoints.hub.HubController.*(..))")
//    public void logApiAccess(JoinPoint joinPoint) {
//        IzgPrincipal principal = RequestContext.getPrincipal();
//        String endpoint = request.getRequestURI();
//        XformApiLogDetail data = new XformApiLogDetail();
//        data.setEventId(RequestContext.getEventId());
//        data.setPath(endpoint);
//        data.setUserName(principal.getName());
//        data.setMethod(request.getMethod());
//        data.setPrincipalType(principal.getClass().getSimpleName());
//        ConfigApiRequestContext.setApiLogDetail(data);
////        log.info(Markers2.append("apiLog", data), "User {} accessed {} with method {}",
////                principal.getName(), endpoint, request.getMethod());
//    }
//}
