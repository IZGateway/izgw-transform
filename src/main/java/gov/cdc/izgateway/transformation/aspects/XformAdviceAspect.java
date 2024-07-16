package gov.cdc.izgateway.transformation.aspects;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.logging.advice.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class XformAdviceAspect {

    @Around("execution(* *(..)) && @annotation(gov.cdc.izgateway.transformation.annotations.CaptureXformAdvice)")
    public Object processXformAdviceAspect(ProceedingJoinPoint joinPoint) throws Throwable {
        addAdvice(joinPoint, MethodDisposition.PREEXECUTION);
        Object response = joinPoint.proceed();
        addAdvice(joinPoint, MethodDisposition.POSTEXECUTION);

        return response;
    }

    private void addAdvice(ProceedingJoinPoint joinPoint, MethodDisposition methodDisposition) throws HL7Exception {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof ServiceContext context) {
                XformAdviceCollector.getTransactionData().addAdvice(createXformAdvice(joinPoint, context, methodDisposition));
                break;
            }
        }
    }

    private XformAdviceRecord createXformAdvice(ProceedingJoinPoint joinPoint, ServiceContext context, MethodDisposition methodDisposition) throws HL7Exception {
        Message responseMessage = context.getResponseMessage();
        String descriptor = "Unknown";
        String descriptorId = "Unknown";
        boolean hasTransformed = false;

        Object targetObject = joinPoint.getTarget();
        if ( targetObject instanceof Advisable advisable ) {
            descriptor = advisable.getName();
            descriptorId = advisable.getId();
            hasTransformed = advisable.hasTransformed();
        }

        String request = getRequestMessage(joinPoint, context, methodDisposition, hasTransformed);
        String response = getResponseMessage(joinPoint, context, methodDisposition, hasTransformed);

        return new XformAdviceRecord(
                joinPoint.getTarget().getClass().getSimpleName(),
                joinPoint.getSignature().getName(),
                methodDisposition,
                descriptor,
                descriptorId,
                request,
                response,
                context.getCurrentDirection(),
                hasTransformed);

    }

    // Return the request message if it's the original message or it has been transformed
    private String getRequestMessage(ProceedingJoinPoint joinPoint, ServiceContext context, MethodDisposition methodDisposition, boolean hasTransformed) throws HL7Exception {
        if ( AdviceUtil.isPipelineAdvice(joinPoint.getTarget().getClass().getSimpleName()) &&
                methodDisposition == MethodDisposition.PREEXECUTION ) {
            return context.getRequestMessage().encode();
        } else if ( methodDisposition == MethodDisposition.POSTEXECUTION && hasTransformed) {
            return context.getRequestMessage().encode();
        } else {
            return null;
        }
    }

    // Return the response message if it's the original message or it has been transformed
    private String getResponseMessage(ProceedingJoinPoint joinPoint, ServiceContext context, MethodDisposition methodDisposition, boolean hasTransformed) throws HL7Exception {
        Message responseMessage = context.getResponseMessage();

        if ( AdviceUtil.isPipelineAdvice(joinPoint.getTarget().getClass().getSimpleName()) &&
                methodDisposition == MethodDisposition.PREEXECUTION ) {
            return responseMessage == null ? null : responseMessage.encode();
        } else if ( methodDisposition == MethodDisposition.POSTEXECUTION && hasTransformed) {
            return responseMessage == null ? null : responseMessage.encode();
        } else {
            return null;
        }
    }

}
