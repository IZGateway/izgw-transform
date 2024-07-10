package gov.cdc.izgateway.transformation.aspects;

import ca.uhn.hl7v2.model.Message;
import gov.cdc.izgateway.transformation.annotations.CaptureXformAdvice;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.logging.advice.Action;
import gov.cdc.izgateway.transformation.logging.advice.XformAdvice;
import gov.cdc.izgateway.transformation.logging.advice.XformAdviceCollector;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class XformAdviceAspect {

    //@Around("@annotation(gov.cdc.izgateway.transformation.annotations.CaptureXformAdvice)")
    @Around("@annotation(gov.cdc.izgateway.transformation.annotations.CaptureXformAdvice)")
    public Object logSolutionExecutionAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        Object response = joinPoint.proceed(); // Continue with the method execution

        // Extract the ServiceContext parameter from the method arguments
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof ServiceContext) {
                ServiceContext context = (ServiceContext) arg;
                Message responseMessage = context.getResponseMessage();
                String configurationName = "Unknown"; // Default name, adjust based on your method to retrieve the actual name

                // Attempt to retrieve configuration name if possible
                try {
                    configurationName = joinPoint.getTarget().getClass().getDeclaredField("configuration").get(joinPoint.getTarget()).toString();
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    // Handle the case where the configuration name cannot be retrieved
                }

                XformAdviceCollector.getTransactionData().addAdvice(
                        new XformAdvice(Action.SOLUTION, "Executing Solution: " + configurationName,
                                context.getRequestMessage().encode(), responseMessage == null ? null : responseMessage.encode()));
            }
        }

        return response;
    }

    @Around("execution(* gov.cdc.izgateway.transformation.endpoints.hub.*.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        Object proceed = joinPoint.proceed(); // Execute the advised method

        long executionTime = System.currentTimeMillis() - start;
        System.out.println(joinPoint.getSignature() + " executed in " + executionTime + "ms");

        return proceed; // Return the result of the advised method
    }

//    @Around("@annotation(gov.cdc.izgateway.transformation.annotations.CaptureXformAdvice)")
//    public Object logExecutionTime2(ProceedingJoinPoint joinPoint) throws Throwable {
//        long start = System.currentTimeMillis();
//
//        Object proceed = joinPoint.proceed(); // Execute the advised method
//
//        long executionTime = System.currentTimeMillis() - start;
//        System.out.println(joinPoint.getSignature() + " executed in " + executionTime + "ms");
//
//        return proceed; // Return the result of the advised method
//    }

}
