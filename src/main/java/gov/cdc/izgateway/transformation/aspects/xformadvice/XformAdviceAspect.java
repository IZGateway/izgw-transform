package gov.cdc.izgateway.transformation.aspects.xformadvice;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.enums.DataFlowDirection;
import gov.cdc.izgateway.transformation.logging.advice.*;
import org.aspectj.lang.ProceedingJoinPoint;
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
        String descriptor = "Unknown";
        String descriptorId = "Unknown";
        boolean hasTransformed = false;

        Object targetObject = joinPoint.getTarget();
        if (targetObject instanceof Advisable advisable) {
            descriptor = advisable.getName();
            descriptorId = advisable.getId();
            hasTransformed = advisable.hasTransformed();
        }

        xformAdvice.setClassName(joinPoint.getTarget().getClass().getSimpleName());
        xformAdvice.setName(descriptor);
        if ( context.getCurrentDirection() == DataFlowDirection.REQUEST ) {
            updateRequestMessage(joinPoint, context, methodDisposition, hasTransformed, xformAdvice);
        } else {
            updateResponseMessage(joinPoint, context, methodDisposition, hasTransformed, xformAdvice);
        }

        if ( xformAdvice instanceof PipelineAdviceDTO pipelineAdvice ) {
            pipelineAdvice.setId(descriptorId);
        } else if ( xformAdvice instanceof SolutionAdviceDTO solutionAdvice ) {
            solutionAdvice.setId(descriptorId);
        }
    }

    private void updateRequestMessage(ProceedingJoinPoint joinPoint, ServiceContext context, MethodDisposition methodDisposition, boolean hasTransformed, XformAdviceDTO advice) throws HL7Exception {
        if ( AdviceUtil.isPipelineAdvice(joinPoint.getTarget().getClass().getSimpleName()) &&
                methodDisposition == MethodDisposition.PREEXECUTION ) {
            advice.setRequest(context.getRequestMessage().encode());
        } else if ( methodDisposition == MethodDisposition.POSTEXECUTION && hasTransformed) {
            advice.setTransformedRequest(context.getRequestMessage().encode());
        }
    }

    private void updateResponseMessage(ProceedingJoinPoint joinPoint, ServiceContext context, MethodDisposition methodDisposition, boolean hasTransformed, XformAdviceDTO advice) throws HL7Exception {
        Message responseMessage = context.getResponseMessage();

        if ( AdviceUtil.isPipelineAdvice(joinPoint.getTarget().getClass().getSimpleName()) &&
                methodDisposition == MethodDisposition.PREEXECUTION ) {
            advice.setResponse(responseMessage == null ? null : responseMessage.encode());
        } else if ( methodDisposition == MethodDisposition.POSTEXECUTION && hasTransformed) {
            advice.setTransformedResponse(responseMessage == null ? null : responseMessage.encode());
        }
    }

}