package gov.cdc.izgateway.transformation.aspects;

import ca.uhn.hl7v2.model.Message;
import gov.cdc.izgateway.transformation.annotations.CaptureXformAdvice;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.logging.advice.Action;
import gov.cdc.izgateway.transformation.logging.advice.Advisable;
import gov.cdc.izgateway.transformation.logging.advice.XformAdvice;
import gov.cdc.izgateway.transformation.logging.advice.XformAdviceCollector;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
//@Component
public class XformAdviceAspect {

    //@Around("@annotation(gov.cdc.izgateway.transformation.annotations.CaptureXformAdvice)")
    @Around("execution(* *(..)) && @annotation(gov.cdc.izgateway.transformation.annotations.CaptureXformAdvice)")
    public Object logSolutionExecutionAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("(PRE) Before method: " + joinPoint.getSignature().getName());
        System.out.println("(PRE) Belongs to class: " + joinPoint.getTarget().getClass().getSimpleName());
        System.out.println("(PRE) In the annoation: " + joinPoint.getTarget().getClass().getSimpleName());
        addAdvice(joinPoint, "(pre-execution)");
        Object response = joinPoint.proceed();
        addAdvice(joinPoint, "(post-execution)");
        System.out.println("(POST) Before method: " + joinPoint.getSignature().getName());
        System.out.println("(POST) Belongs to class: " + joinPoint.getTarget().getClass().getSimpleName());
        System.out.println("(POST) In the annoation: " + joinPoint.getTarget().getClass().getSimpleName());

        return response;
    }

    private void addAdvice(ProceedingJoinPoint joinPoint, String descriptor) throws Throwable {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof ServiceContext) {
                ServiceContext context = (ServiceContext) arg;

                XformAdviceCollector.getTransactionData().addAdvice(createXformAdvice(joinPoint, context, descriptor));
            }
        }

    }

    private XformAdvice createXformAdvice(ProceedingJoinPoint joinPoint, ServiceContext context, String descriptor) throws Throwable {
        Message responseMessage = context.getResponseMessage();
        String name = "Unknown";

        Object targetObject = joinPoint.getTarget();
        if ( targetObject instanceof Advisable ) {
            name = ((Advisable) targetObject).getName();
        }

        return new XformAdvice(
                joinPoint.getTarget().getClass().getSimpleName(),
                joinPoint.getSignature().getName() + " " + descriptor,
                name,
                context.getRequestMessage().encode(),
                responseMessage == null ? null : responseMessage.encode());

    }
}
