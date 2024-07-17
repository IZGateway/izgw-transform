package gov.cdc.izgateway.transformation.logging;

import com.fasterxml.jackson.annotation.JsonInclude;
import gov.cdc.izgateway.logging.event.TransactionData;
import gov.cdc.izgateway.logging.markers.MarkerObjectFieldName;
import gov.cdc.izgateway.logging.markers.Markers2;
import gov.cdc.izgateway.transformation.aspects.xformadvice.*;
import gov.cdc.izgateway.transformation.enums.DataFlowDirection;
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

    private PipelineAdvice pipelineAdvice = null;
    private SolutionAdvice currentSolutionAdvice = null;

    public XformTransactionData() {
        super();
    }

    public XformTransactionData(String eventId) {
        super(eventId);
    }

    public void addAdvice(XformAspectDetail advice) {
        if ( advice == null )
            return;

        if ( advice instanceof PipelineAspectDetail pAdvice)
            addAdvice(pAdvice);
        else if ( advice instanceof SolutionAspectDetail sAdvice)
            addAdvice(sAdvice);
        else if ( advice instanceof OperationAspectDetail oAdvice)
            addAdvice(oAdvice);
        else
            addAdviceOriginal(advice);
    }

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

    public void addAdvice(SolutionAspectDetail advice) {

        currentSolutionAdvice = pipelineAdvice.getSolutionAdvice(advice);

        if ( advice.getMethodDisposition() == MethodDisposition.POSTEXECUTION && advice.isHasTransformed() ) {
            if ( advice.getDataFlowDirection() == DataFlowDirection.REQUEST )
                currentSolutionAdvice.setTransformedRequest(advice.getRequestMessage());
            else
                currentSolutionAdvice.setTransformedResponse(advice.getResponseMessage());
        }
    }

    public void addAdvice(OperationAspectDetail advice) {

        if ( advice.getMethodDisposition() == MethodDisposition.POSTEXECUTION) {
            OperationAdvice operationTransformAdvice = new OperationAdvice(advice.getClassName(), advice.getName());
            if ( advice.getDataFlowDirection() == DataFlowDirection.REQUEST ) {
                operationTransformAdvice.setTransformedRequest(advice.getRequestMessage());
                currentSolutionAdvice.addRequestOperationAdvice(operationTransformAdvice);
            }
            else {
                operationTransformAdvice.setTransformedResponse(advice.getResponseMessage());
                currentSolutionAdvice.addResponseOperationAdvice(operationTransformAdvice);
            }
        }
    }

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
