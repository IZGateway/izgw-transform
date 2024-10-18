package gov.cdc.izgateway.transformation.aspects.xformadvice;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.enums.DataFlowDirection;
import gov.cdc.izgateway.transformation.logging.advice.*;
import gov.cdc.izgateway.transformation.operations.Operation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.util.UUID;

@Aspect
public class XformAdviceAspect {

    @Around("execution(* *(..)) && @annotation(gov.cdc.izgateway.transformation.annotations.CaptureXformAdvice)")
    public Object processXformAdviceAspect(ProceedingJoinPoint joinPoint) throws Throwable {
        boolean hasErrored = false;

        addAdvice(joinPoint, MethodDisposition.PREEXECUTION, hasErrored);

        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            hasErrored = true;
            throw e;
        } finally {
            addAdvice(joinPoint, MethodDisposition.POSTEXECUTION, hasErrored);
        }

    }

    private void addAdvice(ProceedingJoinPoint joinPoint, MethodDisposition methodDisposition, boolean hasErrored) throws HL7Exception {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof ServiceContext context) {
                XformAdviceCollector.getTransactionData().addAdvice(getXformAdvice(joinPoint, context, methodDisposition, hasErrored));
                break;
            }
        }
    }

    private XformAdviceDTO getXformAdvice(ProceedingJoinPoint joinPoint, ServiceContext context, MethodDisposition methodDisposition, boolean hasErrored) throws HL7Exception {
        XformAdviceDTO xformAdvice;

        if (AdviceUtil.isPipelineAdvice(joinPoint.getTarget().getClass().getSimpleName())) {
            xformAdvice = new PipelineAdviceDTO();
        } else if (AdviceUtil.isSolutionAdvice(joinPoint.getTarget().getClass().getSimpleName())) {
            xformAdvice = new SolutionAdviceDTO();
        } else if (joinPoint.getTarget() instanceof Operation) {
            xformAdvice = new OperationAdviceDTO();
        } else {
            return null;
        }

        populateAdvice(xformAdvice, joinPoint, context, methodDisposition, hasErrored);

        return xformAdvice;
    }

    private void populateAdvice(XformAdviceDTO xformAdvice, ProceedingJoinPoint joinPoint, ServiceContext context, MethodDisposition methodDisposition, boolean hasErrored) throws HL7Exception {
        String name = "Unknown";
        UUID id = null;
        boolean hasTransformed = false;

        Object targetObject = joinPoint.getTarget();
        if (targetObject instanceof Advisable advisable) {
            name = advisable.getName();
            id = advisable.getId();
        }

        if (methodDisposition == MethodDisposition.POSTEXECUTION && xformAdvice instanceof PipelineAdviceDTO) {
            hasTransformed = context.getCurrentDirection() == DataFlowDirection.REQUEST
                    ? XformAdviceCollector.getTransactionData().getPipelineAdvice().isRequestTransformed()
                    : XformAdviceCollector.getTransactionData().getPipelineAdvice().isResponseTransformed();
        } else if (targetObject instanceof Transformable transformable) {
            hasTransformed = transformable.hasTransformed();
        }

        xformAdvice.setClassName(joinPoint.getTarget().getClass().getSimpleName());
        xformAdvice.setName(name);
        xformAdvice.setId(id);
        xformAdvice.setProcessError(hasErrored);
        xformAdvice.setOrganizationId(context.getOrganizationId());

        if ( context.getCurrentDirection() == DataFlowDirection.REQUEST ) {
            updateRequestMessage(context, methodDisposition, hasTransformed, xformAdvice);
        } else {
            updateResponseMessage(context, methodDisposition, hasTransformed, xformAdvice);
        }
    }

    private void updateRequestMessage(ServiceContext context, MethodDisposition methodDisposition, boolean hasTransformed, XformAdviceDTO advice) throws HL7Exception {
        if ( advice instanceof PipelineAdviceDTO && methodDisposition == MethodDisposition.PREEXECUTION) {
            advice.setRequest(context.getRequestMessage().encode());
        } else if ( methodDisposition == MethodDisposition.POSTEXECUTION && hasTransformed) {
            advice.setTransformedRequest(context.getRequestMessage().encode());
        }
    }

    private void updateResponseMessage(ServiceContext context, MethodDisposition methodDisposition, boolean hasTransformed, XformAdviceDTO advice) throws HL7Exception {
        Message responseMessage = context.getResponseMessage();

        if ( advice instanceof PipelineAdviceDTO && methodDisposition == MethodDisposition.PREEXECUTION ) {
            advice.setResponse(responseMessage == null ? null : responseMessage.encode());
        } else if ( methodDisposition == MethodDisposition.POSTEXECUTION && hasTransformed) {
            advice.setTransformedResponse(responseMessage == null ? null : responseMessage.encode());
        }
    }

}
