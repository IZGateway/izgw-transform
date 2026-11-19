package gov.cdc.izgateway.xform.logging;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import gov.cdc.izgateway.logging.event.TransactionData;
import gov.cdc.izgateway.logging.markers.MarkerObjectFieldName;
import gov.cdc.izgateway.logging.markers.Markers2;
import gov.cdc.izgateway.xform.logging.advice.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * Transaction data of the Xform Service, with the advice collected for the pipeline that ran.
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
@MarkerObjectFieldName("transactionData")
@JsonInclude(JsonInclude.Include.ALWAYS)
public class XformTransactionData extends TransactionData {
    private static final String XFORM_TRANSACTION_DATA = "XformTransactionData";

    // TODO @JsonIgnore
    private PipelineAdvice pipelineAdvice = null;

    @JsonIgnore
    private SolutionAdvice currentSolutionAdvice = null;

    /**
     * Creates transaction data with a new event identifier.
     */
    public XformTransactionData() {
        super();
    }

    /**
     * Creates transaction data for the event.
     *
     * @param eventId the event identifier
     */
    public XformTransactionData(String eventId) {
        super(eventId);
    }

    /**
     * Adds advice to the collected advice for its type. Null advice is ignored.
     *
     * @param advice the advice to add
     */
    public void addAdvice(XformAdviceDTO advice) {
        if ( advice == null )
            return;

        if ( advice instanceof PipelineAdviceDTO pAdvice)
            addAdvice(pAdvice);
        else if ( advice instanceof SolutionAdviceDTO sAdvice)
            addAdvice(sAdvice);
        else if ( advice instanceof OperationAdviceDTO oAdvice)
            addAdvice(oAdvice);
        else if ( advice instanceof PreconditionAdviceDTO preAdvice )
            addAdvice(preAdvice);
    }

    /**
     * Creates the pipeline advice if necessary and updates it from the given advice.
     *
     * @param advice the pipeline advice
     */
    public void addAdvice(PipelineAdviceDTO advice) {

        if ( pipelineAdvice == null ) {
            pipelineAdvice = new PipelineAdvice(advice);
        }

        pipelineAdvice.setName(advice.getName());
        pipelineAdvice.setId(advice.getId());
        pipelineAdvice.setProcessError(advice.isProcessError());
        pipelineAdvice.setOrganizationId(advice.getOrganizationId());
        if ( advice.getTransformedRequest() != null)
            pipelineAdvice.setTransformedRequest(advice.getTransformedRequest());
        if ( advice.getResponse() != null )
            pipelineAdvice.setResponse(advice.getResponse());
        if ( advice.getTransformedResponse() != null )
            pipelineAdvice.setTransformedResponse(advice.getTransformedResponse());
    }

    /**
     * Makes the solution the current solution and records its errors and transformed messages.
     *
     * @param advice the solution advice
     */
    public void addAdvice(SolutionAdviceDTO advice) {

        currentSolutionAdvice = pipelineAdvice.getSolutionAdvice(advice);
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

    /**
     * Adds operation advice to the current solution if the operation transformed the request or the response.
     *
     * @param advice the operation advice
     */
    public void addAdvice(OperationAdviceDTO advice) {
        if (advice.getTransformedRequest() != null)
            currentSolutionAdvice.addRequestOperationAdvice(advice);
        if (advice.getTransformedResponse() != null)
            currentSolutionAdvice.addResponseOperationAdvice(advice);

    }

    /**
     * Adds precondition advice to the pipeline if no solution has run, otherwise to the current solution.
     *
     * @param advice the precondition advice
     */
    public void addAdvice(PreconditionAdviceDTO advice) {
        if (currentSolutionAdvice == null) {
            // There isn't a solution seen yet... so we are at the pipeline level
            // Add the Precondition if it isn't here
            pipelineAdvice.addPreconditionAdvice(advice);
        } else {
            // There is a current solution...
            // Check to make sure this wasn't saved on the pipeline first
            if (!pipelineAdvice.preconditionExists(advice)) {

                if (advice.getTransformedRequest() != null) {
                    currentSolutionAdvice.addRequestPreconditionAdvice(advice);
                }

                if (advice.getTransformedResponse() != null) {
                    currentSolutionAdvice.addResponsePreconditionAdvice(advice);
                }
            }
        }

    }

    @Override
    public void logIt() {
        computeTransactionTimes();
        log.info(Markers2.append("transactionData", this), "{}", getMessage());
        logPipelineDetail();
        logSolutionDetail();
    }

    private void logPipelineDetail() {
    	if (pipelineAdvice == null) {
    		return;
    	}
        XformLogDetail logDetail = createLogDetail("pipeline", pipelineAdvice.getName());

        log.info(Markers2.append(XFORM_TRANSACTION_DATA, logDetail), "{}", "");
    }

    private void logSolutionDetail() {
    	if (pipelineAdvice == null) {
    		return;
    	}
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
        logDetail.setDestinationId(this.getDestination().getId());
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
