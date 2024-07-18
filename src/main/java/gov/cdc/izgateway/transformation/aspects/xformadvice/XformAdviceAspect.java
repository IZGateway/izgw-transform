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

    /*
     * The next refactor is to simplify the data structures to just use XformAdvice and not have OperationAspectDetail for example.
     * We also want to try to set the transformedRequest in the aspect as we have what we need right there.
     */
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

    private XformAdvice createXformAdvice(ProceedingJoinPoint joinPoint, ServiceContext context, MethodDisposition methodDisposition) throws HL7Exception {
        if (AdviceUtil.isPipelineAdvice(joinPoint.getTarget().getClass().getSimpleName())) {
            return createPipelineAdvice(joinPoint, context, methodDisposition);
        } else if (AdviceUtil.isSolutionAdvice(joinPoint.getTarget().getClass().getSimpleName())) {
            return createSolutionAdvice(joinPoint, context, methodDisposition);
        } else if (AdviceUtil.isOperationAdvice(joinPoint.getTarget().getClass().getSimpleName())) {
            return createOperationAdvice(joinPoint, context, methodDisposition);
        } else {
            return null;
        }
    }

//    private OperationAspectDetail createOperationAdvice(ProceedingJoinPoint joinPoint, ServiceContext context, MethodDisposition methodDisposition) throws HL7Exception {
//        String descriptor = "Unknown";
//        String descriptorId = "Unknown";
//        boolean hasTransformed = false;
//
//        Object targetObject = joinPoint.getTarget();
//        if ( targetObject instanceof Advisable advisable ) {
//            descriptor = advisable.getName();
//            descriptorId = advisable.getId();
//            hasTransformed = advisable.hasTransformed();
//        }
//
//        String request = getRequestMessage(joinPoint, context, methodDisposition, hasTransformed);
//        String response = getResponseMessage(joinPoint, context, methodDisposition, hasTransformed);
//
//        return new OperationAspectDetail(
//                hasTransformed,
//                descriptor,
//                joinPoint.getTarget().getClass().getSimpleName(),
//                joinPoint.getSignature().getName(),
//                methodDisposition,
//                request,
//                response,
//                context.getCurrentDirection());
//
//    }

    private PipelineAdviceDTO createPipelineAdvice(ProceedingJoinPoint joinPoint, ServiceContext context, MethodDisposition methodDisposition) throws HL7Exception {
        PipelineAdviceDTO pipelineAdvice = new PipelineAdviceDTO();

        extractCommonAttributes(pipelineAdvice, joinPoint, context, methodDisposition);

        return pipelineAdvice;
    }

    private SolutionAdviceDTO createSolutionAdvice(ProceedingJoinPoint joinPoint, ServiceContext context, MethodDisposition methodDisposition) throws HL7Exception {
        SolutionAdviceDTO solutionAdvice = new SolutionAdviceDTO();

        extractCommonAttributes(solutionAdvice, joinPoint, context, methodDisposition);

        return solutionAdvice;
    }

    private OperationAdviceDTO createOperationAdvice(ProceedingJoinPoint joinPoint, ServiceContext context, MethodDisposition methodDisposition) throws HL7Exception {
        // We only care about post execution for OperationAdvice
        if ( methodDisposition != MethodDisposition.POSTEXECUTION ) {
            return null;
        }

        OperationAdviceDTO operationAdvice = new OperationAdviceDTO();

        extractCommonAttributes(operationAdvice, joinPoint, context, methodDisposition);

        return operationAdvice;
    }

    private void extractCommonAttributes(XformAdvice xformAdvice, ProceedingJoinPoint joinPoint, ServiceContext context, MethodDisposition methodDisposition) throws HL7Exception {
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
//    private SolutionAspectDetail createSolutionAdvice(ProceedingJoinPoint joinPoint, ServiceContext context, MethodDisposition methodDisposition) throws HL7Exception {
//        String descriptor = "Unknown";
//        String descriptorId = "Unknown";
//        boolean hasTransformed = false;
//
//        Object targetObject = joinPoint.getTarget();
//        if ( targetObject instanceof Advisable advisable ) {
//            descriptor = advisable.getName();
//            descriptorId = advisable.getId();
//            hasTransformed = advisable.hasTransformed();
//        }
//
//        String request = getRequestMessage(joinPoint, context, methodDisposition, hasTransformed);
//        String response = getResponseMessage(joinPoint, context, methodDisposition, hasTransformed);
//
//        return new SolutionAspectDetail(
//                descriptorId,
//                hasTransformed,
//                descriptor,
//                joinPoint.getTarget().getClass().getSimpleName(),
//                joinPoint.getSignature().getName(),
//                methodDisposition,
//                request,
//                response,
//                context.getCurrentDirection());
//
//    }
//

    /*
    public void addAdvice(PipelineAspectDetail advice) {

        if ( advice.getDataFlowDirection() == DataFlowDirection.REQUEST ) {
            if ( advice.getMethodDisposition() == MethodDisposition.PREEXECUTION ) {
                pipelineAdvice = new PipelineAdvice(advice.getId(), advice.getClassName(), advice.getName());
                pipelineAdvice.setRequest(advice.getRequestMessage());
            }
            else
                pipelineAdvice.setTransformedRequest(advice.getRequestMessage());
        } else {
            if ( advice.getMethodDisposition() == MethodDisposition.PREEXECUTION )
                pipelineAdvice.setResponse(advice.getResponseMessage());
            else
                pipelineAdvice.setTransformedResponse(advice.getResponseMessage());
        }
    }

     */


//    private PipelineAspectDetail createPipelineAdviceOld(ProceedingJoinPoint joinPoint, ServiceContext context, MethodDisposition methodDisposition) throws HL7Exception {
//        String descriptor = "Unknown";
//        String descriptorId = "Unknown";
//        boolean hasTransformed = false;
//
//        Object targetObject = joinPoint.getTarget();
//        if ( targetObject instanceof Advisable advisable ) {
//            descriptor = advisable.getName();
//            descriptorId = advisable.getId();
//            hasTransformed = advisable.hasTransformed();
//        }
//
//        String request = getRequestMessage(joinPoint, context, methodDisposition, hasTransformed);
//        String response = getResponseMessage(joinPoint, context, methodDisposition, hasTransformed);
//
//        return new PipelineAspectDetail(
//                descriptorId,
//                descriptor,
//                joinPoint.getTarget().getClass().getSimpleName(),
//                joinPoint.getSignature().getName(),
//                methodDisposition,
//                request,
//                response,
//                context.getCurrentDirection());
//
//    }


    private void updateRequestMessage(ProceedingJoinPoint joinPoint, ServiceContext context, MethodDisposition methodDisposition, boolean hasTransformed, XformAdvice advice) throws HL7Exception {
        if ( AdviceUtil.isPipelineAdvice(joinPoint.getTarget().getClass().getSimpleName()) &&
                methodDisposition == MethodDisposition.PREEXECUTION ) {
            advice.setRequest(context.getRequestMessage().encode());
        } else if ( methodDisposition == MethodDisposition.POSTEXECUTION && hasTransformed) {
            advice.setTransformedRequest(context.getRequestMessage().encode());
        }
    }

    private String getRequestMessageOriginal(ProceedingJoinPoint joinPoint, ServiceContext context, MethodDisposition methodDisposition, boolean hasTransformed) throws HL7Exception {
        if ( AdviceUtil.isPipelineAdvice(joinPoint.getTarget().getClass().getSimpleName()) &&
                methodDisposition == MethodDisposition.PREEXECUTION ) {
            return context.getRequestMessage().encode();
        } else if ( methodDisposition == MethodDisposition.POSTEXECUTION && hasTransformed) {
            return context.getRequestMessage().encode();
        } else {
            return null;
        }
    }

    private void updateResponseMessage(ProceedingJoinPoint joinPoint, ServiceContext context, MethodDisposition methodDisposition, boolean hasTransformed, XformAdvice advice) throws HL7Exception {
        Message responseMessage = context.getResponseMessage();

        if ( AdviceUtil.isPipelineAdvice(joinPoint.getTarget().getClass().getSimpleName()) &&
                methodDisposition == MethodDisposition.PREEXECUTION ) {
            advice.setResponse(responseMessage == null ? null : responseMessage.encode());
        } else if ( methodDisposition == MethodDisposition.POSTEXECUTION && hasTransformed) {
            advice.setTransformedResponse(responseMessage == null ? null : responseMessage.encode());
        }
    }

    private String getResponseMessageOriginal(ProceedingJoinPoint joinPoint, ServiceContext context, MethodDisposition methodDisposition, boolean hasTransformed) throws HL7Exception {
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