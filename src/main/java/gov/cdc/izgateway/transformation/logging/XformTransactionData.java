package gov.cdc.izgateway.transformation.logging;

import com.fasterxml.jackson.annotation.JsonInclude;
import gov.cdc.izgateway.logging.event.TransactionData;
import gov.cdc.izgateway.logging.markers.MarkerObjectFieldName;
import gov.cdc.izgateway.logging.markers.Markers2;
import gov.cdc.izgateway.transformation.aspects.xformadvice.*;
import gov.cdc.izgateway.transformation.logging.advice.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

@Slf4j
@Data
@MarkerObjectFieldName("transactionData")
@JsonInclude(JsonInclude.Include.ALWAYS)
public class XformTransactionData extends TransactionData {

    // TODO Add @JsonIgnore as we don't with these to be wrriten to the log!!! PHI!
    private ArrayList<XformAspectDetail> xformAdviceList = new ArrayList<>();

    private PipelineAdvicePlus pipelineAdvicePlus = null;
    private SolutionAdvicePlus currentSolutionAdvice = null;

    public XformTransactionData() {
        super();
    }

    public XformTransactionData(String eventId) {
        super(eventId);
    }

    public void addAdvice(XformAdvice advice) {
        if ( advice == null )
            return;

        if ( advice instanceof PipelineAdvice pAdvice)
            addAdvice(pAdvice);
        else if ( advice instanceof SolutionAdvice sAdvice)
            addAdvice(sAdvice);
        else if ( advice instanceof OperationAdvice oAdvice)
            addAdvice(oAdvice);
    }

    public void addAdvice(PipelineAdvice advice) {

        if ( pipelineAdvicePlus == null ) {
            pipelineAdvicePlus = new PipelineAdvicePlus();
            pipelineAdvicePlus.setPipelineAdvice(advice);
        } else {
            if ( advice.getTransformedRequest() != null)
                pipelineAdvicePlus.getPipelineAdvice().setTransformedRequest(advice.getTransformedRequest());
            if ( advice.getResponse() != null )
                pipelineAdvicePlus.getPipelineAdvice().setResponse(advice.getResponse());
            if ( advice.getTransformedResponse() != null )
                pipelineAdvicePlus.getPipelineAdvice().setTransformedResponse(advice.getTransformedResponse());
        }

    }

    public void addAdvice(SolutionAdvice advice) {

        currentSolutionAdvice = pipelineAdvicePlus.getSolutionAdvice(advice);

        if ( advice.getTransformedRequest() != null )
            currentSolutionAdvice.setTransformedRequest(advice.getTransformedRequest());
        if ( advice.getTransformedResponse() != null )
            currentSolutionAdvice.setTransformedResponse(advice.getTransformedResponse());
    }

    public void addAdvice(OperationAdvice advice) {
        if ( advice.getTransformedRequest() != null )
            currentSolutionAdvice.addRequestOperationAdvice(advice);
        if ( advice.getTransformedResponse() != null )
            currentSolutionAdvice.addResponseOperationAdvice(advice);

//        if ( advice.getMethodDisposition() == MethodDisposition.POSTEXECUTION) {
//            OperationAdvice operationTransformAdvice = new OperationAdvice(advice.getClassName(), advice.getName());
//            if ( advice.getDataFlowDirection() == DataFlowDirection.REQUEST ) {
//                operationTransformAdvice.setTransformedRequest(advice.getRequestMessage());
//                currentSolutionAdvice.addRequestOperationAdvice(operationTransformAdvice);
//            }
//            else {
//                operationTransformAdvice.setTransformedResponse(advice.getResponseMessage());
//                currentSolutionAdvice.addResponseOperationAdvice(operationTransformAdvice);
//            }
//        }
    }

//    public void addAdviceOriginal(OperationAspectDetail advice) {
//
//        if ( advice.getMethodDisposition() == MethodDisposition.POSTEXECUTION) {
//            OperationAdvice operationTransformAdvice = new OperationAdvice(advice.getClassName(), advice.getName());
//            if ( advice.getDataFlowDirection() == DataFlowDirection.REQUEST ) {
//                operationTransformAdvice.setTransformedRequest(advice.getRequestMessage());
//                currentSolutionAdvice.addRequestOperationAdvice(operationTransformAdvice);
//            }
//            else {
//                operationTransformAdvice.setTransformedResponse(advice.getResponseMessage());
//                currentSolutionAdvice.addResponseOperationAdvice(operationTransformAdvice);
//            }
//        }
//    }

    public void addAdviceOriginal(XformAspectDetail advice) {

//        if ( AdviceUtil.isPipelineAdvice(advice.className()) ) {
//            if ( advice.dataFlowDirection() == DataFlowDirection.REQUEST ) {
//                if ( advice.methodDisposition() == MethodDisposition.PREEXECUTION ) {
//                    pipelineAdvice = new PipelineAdvice(advice.descriptorId(), advice.className(), advice.descriptor());
//                    pipelineAdvice.setRequest(advice.requestMessage());
//                }
//                else
//                    pipelineAdvice.setTransformedRequest(advice.requestMessage());
//            } else {
//                if ( advice.methodDisposition() == MethodDisposition.PREEXECUTION )
//                    pipelineAdvice.setResponse(advice.responseMessage());
//                else
//                    pipelineAdvice.setTransformedResponse(advice.responseMessage());
//            }
//            return;
//        }
//
//
//        if ( AdviceUtil.isSolutionAdvice(advice.className()) ) {
//            // get the solution by ID
//            currentSolutionAdvice = pipelineAdvice.getSolutionAdvice(advice);
//
//            if ( advice.methodDisposition() == MethodDisposition.POSTEXECUTION && advice.hasTransformed() ) {
//                if ( advice.dataFlowDirection() == DataFlowDirection.REQUEST )
//                    currentSolutionAdvice.setTransformedRequest(advice.requestMessage());
//                else
//                    currentSolutionAdvice.setTransformedResponse(advice.responseMessage());
//            }
//
//            return;
//        }
//
//        if ( advice.className().contains("Operation") && advice.methodDisposition() == MethodDisposition.POSTEXECUTION) {
//            OperationAdvice operationTransformAdvice = new OperationAdvice(advice.className(), advice.descriptor());
//            if ( advice.dataFlowDirection() == DataFlowDirection.REQUEST ) {
//                operationTransformAdvice.setTransformedRequest(advice.requestMessage());
//                currentSolutionAdvice.addRequestOperationAdvice(operationTransformAdvice);
//            }
//            else {
//                operationTransformAdvice.setTransformedResponse(advice.responseMessage());
//                currentSolutionAdvice.addResponseOperationAdvice(operationTransformAdvice);
//            }
//            return;
//        }

    }




    @Override
    public void logIt() {
        computeTransactionTimes();
        log.info(Markers2.append("transactionData", this), "{}", getMessage());
    }
}
