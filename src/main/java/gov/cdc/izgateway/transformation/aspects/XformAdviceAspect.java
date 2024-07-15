package gov.cdc.izgateway.transformation.aspects;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.logging.advice.Advisable;
import gov.cdc.izgateway.transformation.logging.advice.MethodDisposition;
import gov.cdc.izgateway.transformation.logging.advice.XformAdviceRecord;
import gov.cdc.izgateway.transformation.logging.advice.XformAdviceCollector;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class XformAdviceAspect {

    // TODO - consider just doing before - not around
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
                // createXformAdvice(joinPoint, context, methodDisposition);
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

        return new XformAdviceRecord(
                joinPoint.getTarget().getClass().getSimpleName(),
                joinPoint.getSignature().getName(),
                methodDisposition,
                descriptor,
                descriptorId,
                context.getRequestMessage().encode(),
                responseMessage == null ? null : responseMessage.encode(),
                context.getCurrentDirection(),
                hasTransformed);

    }
}
