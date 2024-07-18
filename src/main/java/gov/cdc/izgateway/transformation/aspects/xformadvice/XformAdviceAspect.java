package gov.cdc.izgateway.transformation.aspects.xformadvice;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.enums.DataFlowDirection;
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
                XformAdviceCollector.getTransactionData().addAdvice(getXformAdvice(joinPoint, context, methodDisposition));
                break;
            }
        }
    }

    private XformAdviceDTO getXformAdvice(ProceedingJoinPoint joinPoint, ServiceContext context, MethodDisposition methodDisposition) throws HL7Exception {
        XformAdviceDTO xformAdvice = null;

        if (AdviceUtil.isPipelineAdvice(joinPoint.getTarget().getClass().getSimpleName())) {
            xformAdvice = new PipelineAdviceDTO();
        } else if (AdviceUtil.isSolutionAdvice(joinPoint.getTarget().getClass().getSimpleName())) {
            xformAdvice = new SolutionAdviceDTO();
        } else if (AdviceUtil.isOperationAdvice(joinPoint.getTarget().getClass().getSimpleName())) {
            xformAdvice = new OperationAdviceDTO();
        } else {
            return null;
        }

        populateAdvice(xformAdvice, joinPoint, context, methodDisposition);

        return xformAdvice;
    }

    private void populateAdvice(XformAdviceDTO xformAdvice, ProceedingJoinPoint joinPoint, ServiceContext context, MethodDisposition methodDisposition) throws HL7Exception {
        String name = "Unknown";
        String id = "Unknown";
        boolean hasTransformed = false;

        Object targetObject = joinPoint.getTarget();
        if (targetObject instanceof Advisable advisable) {
            name = advisable.getName();
            id = advisable.getId();
            hasTransformed = advisable.hasTransformed();
        }

        xformAdvice.setClassName(joinPoint.getTarget().getClass().getSimpleName());
        xformAdvice.setName(name);
        if ( context.getCurrentDirection() == DataFlowDirection.REQUEST ) {
            updateRequestMessage(joinPoint, context, methodDisposition, hasTransformed, xformAdvice);
        } else {
            updateResponseMessage(joinPoint, context, methodDisposition, hasTransformed, xformAdvice);
        }

        // TODO There may be some refactoring Austin and Paul are working on that may include an "Id" for an Operation which means
        // we could get away with a single XformAdvice object with no class needed to extend it.
        if ( xformAdvice instanceof PipelineAdviceDTO pipelineAdvice ) {
            pipelineAdvice.setId(id);
        } else if ( xformAdvice instanceof SolutionAdviceDTO solutionAdvice ) {
            solutionAdvice.setId(id);
        }
    }

    private void updateRequestMessage(ProceedingJoinPoint joinPoint, ServiceContext context, MethodDisposition methodDisposition, boolean hasTransformed, XformAdviceDTO advice) throws HL7Exception {
        if ( advice instanceof PipelineAdviceDTO && methodDisposition == MethodDisposition.PREEXECUTION) {
            advice.setRequest(context.getRequestMessage().encode());
        } else if ( methodDisposition == MethodDisposition.POSTEXECUTION && hasTransformed) {
            advice.setTransformedRequest(context.getRequestMessage().encode());
        }
    }

    private void updateResponseMessage(ProceedingJoinPoint joinPoint, ServiceContext context, MethodDisposition methodDisposition, boolean hasTransformed, XformAdviceDTO advice) throws HL7Exception {
        Message responseMessage = context.getResponseMessage();

        if ( advice instanceof PipelineAdviceDTO && methodDisposition == MethodDisposition.PREEXECUTION ) {
            advice.setResponse(responseMessage == null ? null : responseMessage.encode());
        } else if ( methodDisposition == MethodDisposition.POSTEXECUTION && hasTransformed) {
            advice.setTransformedResponse(responseMessage == null ? null : responseMessage.encode());
        }
    }

}