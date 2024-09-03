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
    private static final String XFORM_TRANSACTION_DATA = "XformTransactionData";

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
        pipelineAdvice.setDestinationId(advice.getDestinationId());
        pipelineAdvice.setOrganizationId(advice.getOrganizationId());
        if ( advice.getTransformedRequest() != null)
            pipelineAdvice.setTransformedRequest(advice.getTransformedRequest());
        if ( advice.getResponse() != null )
            pipelineAdvice.setResponse(advice.getResponse());
        if ( advice.getTransformedResponse() != null )
            pipelineAdvice.setTransformedResponse(advice.getTransformedResponse());
    }

    public void addAdvice(SolutionAdviceDTO advice) {

        currentSolutionAdvice = pipelineAdvice.getSolutionAdvice(advice);
        currentSolutionAdvice.setDestinationId(advice.getDestinationId());
        currentSolutionAdvice.setOrganizationId(advice.getOrganizationId());

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
        if (advice.getTransformedRequest() != null)
            currentSolutionAdvice.addRequestOperationAdvice(advice);
        if (advice.getTransformedResponse() != null)
            currentSolutionAdvice.addResponseOperationAdvice(advice);

    }

    @Override
    public void logIt() {
        computeTransactionTimes();
        log.info(Markers2.append("transactionData", this), "{}", getMessage());
        logPipelineDetail();
        logSolutionDetail();
    }

    private void logPipelineDetail() {
        XformLogDetail logDetail = createLogDetail("pipeline", pipelineAdvice.getName());

        log.info(Markers2.append(XFORM_TRANSACTION_DATA, logDetail), "{}", "");
    }

    private void logSolutionDetail() {
        pipelineAdvice.getSolutionAdviceList().forEach(s -> {
            XformSolutionLogDetail logDetail = createSolutionLogDetail("solution", s.getName());

            logDetail.setProcessError(s.isProcessError());
            logDetail.setPipelineName(pipelineAdvice.getName());

            if (!s.getRequestOperationAdviceList().isEmpty()) {
                logDetail.setDirection("request");
                log.info(Markers2.append(XFORM_TRANSACTION_DATA, logDetail), "{}", "");
            }

            if (!s.getResponseOperationAdviceList().isEmpty()) {
                logDetail.setDirection("response");
                log.info(Markers2.append(XFORM_TRANSACTION_DATA, logDetail), "{}", "");
            }
        });
    }

    private XformLogDetail createLogDetail(String concept, String name) {
        XformLogDetail logDetail = new XformLogDetail();
        return initializeLogDetail(logDetail, concept, name);
    }

    private XformSolutionLogDetail createSolutionLogDetail(String concept, String name) {
        XformSolutionLogDetail logDetail = new XformSolutionLogDetail();
        initializeLogDetail(logDetail, concept, name);
        return logDetail;
    }

    private XformLogDetail initializeLogDetail(XformLogDetail logDetail, String concept, String name) {
        logDetail.setEventId(this.getEventId());
        logDetail.setConcept(concept);
        logDetail.setDestinationId(this.getDestination().getId() + "PAUL");
        logDetail.setOrganizationId(pipelineAdvice.getOrganizationId());
        logDetail.setName(name);
        logDetail.setProcessError(this.getHasProcessError());
        logDetail.setRequestMessageType(this.getRequestPayloadType().toString());
        logDetail.setRequestSendingApplication(this.getRequestMsh3());
        logDetail.setRequestSendingFacility(this.getRequestMsh4());
        logDetail.setRequestReceivingApplication(this.getRequestMsh5());
        logDetail.setRequestReceivingFacility(this.getRequestMsh6());
        logDetail.setResponseSendingApplication(this.getResponseMsh3());
        logDetail.setResponseSendingFacility(this.getResponseMsh4());
        logDetail.setResponseReceivingApplication(this.getResponseMsh5());
        logDetail.setResponseReceivingFacility(this.getResponseMsh6());

        return logDetail;
    }
}
