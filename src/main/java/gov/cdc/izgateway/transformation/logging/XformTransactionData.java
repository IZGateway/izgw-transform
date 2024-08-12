package gov.cdc.izgateway.transformation.logging;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import gov.cdc.izgateway.logging.event.TransactionData;
import gov.cdc.izgateway.logging.markers.MarkerObjectFieldName;
import gov.cdc.izgateway.logging.markers.Markers2;
import gov.cdc.izgateway.transformation.logging.advice.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@MarkerObjectFieldName("transactionData")
@JsonInclude(JsonInclude.Include.ALWAYS)
public class XformTransactionData extends TransactionData {

    // TODO @JsonIgnore
    private PipelineAdvice pipelineAdvice = null;

    @JsonIgnore
    private SolutionAdvice currentSolutionAdvice = null;

    public XformTransactionData() {
        super();
    }

    public XformTransactionData(String eventId) {
        super(eventId);
    }

    public void addAdvice(XformAdviceDTO advice) {
        if ( advice == null )
            return;

        if ( advice instanceof PipelineAdviceDTO pAdvice)
            addAdvice(pAdvice);
        else if ( advice instanceof SolutionAdviceDTO sAdvice)
            addAdvice(sAdvice);
        else if ( advice instanceof OperationAdviceDTO oAdvice)
            addAdvice(oAdvice);
    }

    public void addAdvice(PipelineAdviceDTO advice) {

        if ( pipelineAdvice == null ) {
            pipelineAdvice = new PipelineAdvice(advice);
        }

        pipelineAdvice.setName(advice.getName());
        pipelineAdvice.setId(advice.getId());
        pipelineAdvice.setProcessError(advice.isProcessError());
        if ( advice.getTransformedRequest() != null)
            pipelineAdvice.setTransformedRequest(advice.getTransformedRequest());
        if ( advice.getResponse() != null )
            pipelineAdvice.setResponse(advice.getResponse());
        if ( advice.getTransformedResponse() != null )
            pipelineAdvice.setTransformedResponse(advice.getTransformedResponse());
    }

    public void addAdvice(SolutionAdviceDTO advice) {

        currentSolutionAdvice = pipelineAdvice.getSolutionAdvice(advice);

        if ( advice.isProcessError() ) {
            currentSolutionAdvice.setProcessError(true);
        }

        if ( advice.getTransformedRequest() != null ) {
            currentSolutionAdvice.setTransformedRequest(advice.getTransformedRequest());
            pipelineAdvice.setRequestTransformed(true);
        }
        if ( advice.getTransformedResponse() != null ) {
            currentSolutionAdvice.setTransformedResponse(advice.getTransformedResponse());
            pipelineAdvice.setResponseTransformed(true);
        }
    }

    public void addAdvice(OperationAdviceDTO advice) {
        if ( advice.getTransformedRequest() != null )
            currentSolutionAdvice.addRequestOperationAdvice(advice);
        if ( advice.getTransformedResponse() != null )
            currentSolutionAdvice.addResponseOperationAdvice(advice);

    }



    @Override
    public void logIt() {
        computeTransactionTimes();
        log.info(Markers2.append("transactionData", this), "{}", getMessage());
        logPipelineDetail();
        logSolutionDetail();
    }

    public void logPipelineDetail() {
        XformLogDetail logDetail = new XformLogDetail();

        logDetail.setEventId(this.getEventId());
        logDetail.setConcept("pipeline");
        logDetail.setName(pipelineAdvice.getName());
        logDetail.setProcessError(this.getHasProcessError());

        log.info(Markers2.append("pipelineDetail", logDetail), "{}", "");
    }

    public void logSolutionDetail() {

        pipelineAdvice.getSolutionAdviceList().forEach(s -> {
            XformSolutionLogDetail logDetail = new XformSolutionLogDetail();

            logDetail.setEventId(this.getEventId());
            logDetail.setConcept("solution");
            logDetail.setName(s.getName());
            logDetail.setProcessError(s.isProcessError());

            if (!s.getRequestOperationAdviceList().isEmpty()) {
                logDetail.setDirection("request");
                log.info(Markers2.append("solutionDetail", logDetail), "{}", "");
            }

            if (!s.getResponseOperationAdviceList().isEmpty()) {
                logDetail.setDirection("response");
                log.info(Markers2.append("solutionDetail", logDetail), "{}", "");
            }
        });
    }
}
