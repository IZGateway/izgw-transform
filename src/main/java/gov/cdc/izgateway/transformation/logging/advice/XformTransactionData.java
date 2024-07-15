package gov.cdc.izgateway.transformation.logging.advice;

import com.fasterxml.jackson.annotation.JsonInclude;
import gov.cdc.izgateway.logging.event.TransactionData;
import gov.cdc.izgateway.logging.markers.MarkerObjectFieldName;
import gov.cdc.izgateway.logging.markers.Markers2;
import gov.cdc.izgateway.transformation.enums.DataFlowDirection;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Data
@MarkerObjectFieldName("transactionData")
@JsonInclude(JsonInclude.Include.ALWAYS)
public class XformTransactionData extends TransactionData {

    // TODO Add @JsonIgnore as we don't with these to be wrriten to the log!!! PHI!
    private ArrayList<XformAdviceRecord> xformAdviceList = new ArrayList<>();

    // TODO - Paul to add logic to change addAdvice to create a structure that we documented in the PowerPoint template
    private Map<String, XformAdvice> xformAdviceMap = new LinkedHashMap<>();


    public XformTransactionData() {
        super();
    }

    public XformTransactionData(String eventId) {
        super(eventId);
    }

    public void addAdvice(XformAdviceRecord advice) {
        xformAdviceList.add(advice);
        updateSolutionAdvice(advice);
        if ( advice.className().equals("Hl7Pipeline") && advice.dataFlowDirection() == DataFlowDirection.RESPONSE && advice.methodDisposition() == MethodDisposition.POSTEXECUTION) {
            reduceAdviceList();
        }
    }

    private void reduceAdviceList() {
        log.info("*** Reducing the advice list. ***");
        for (XformAdviceRecord advice : xformAdviceList) {
//            if ( advice.className().equals("Hl7Pipeline") && advice.dataFlowDirection() == DataFlowDirection.REQUEST && advice.methodDisposition() == MethodDisposition.PREEXECUTION) {
//                log.info("The original request: " + advice.requestMessage());
//            }
//
//            if ( advice.className().equals("Hl7Pipeline") && advice.dataFlowDirection() == DataFlowDirection.REQUEST && advice.methodDisposition() == MethodDisposition.POSTEXECUTION) {
//                log.info("The transformed request: " + advice.requestMessage());
//            }
            if ( advice.className().equals("Hl7Pipeline") && advice.dataFlowDirection() == DataFlowDirection.REQUEST && advice.methodDisposition() == MethodDisposition.PREEXECUTION) {
                log.info("The original request: " + advice.requestMessage());
                continue;
            }

            if ( advice.className().equals("Hl7Pipeline") && advice.dataFlowDirection() == DataFlowDirection.RESPONSE && advice.methodDisposition() == MethodDisposition.PREEXECUTION) {
                continue;
            }

            if ( advice.className().equals("Hl7Pipeline") && advice.dataFlowDirection() == DataFlowDirection.REQUEST && advice.methodDisposition() == MethodDisposition.POSTEXECUTION) {
                log.info("The transformed request: " + advice.requestMessage());
                continue;
            }

            log.info("ID: " + advice.descriptorId() + " name: " + advice.descriptor() + " className: " + advice.className());

            if ( advice.hasTransformed() ) {
                if ( advice.dataFlowDirection() == DataFlowDirection.REQUEST )
                    log.info(advice.className() + " has transformed the request to: " + advice.requestMessage());
                else if ( advice.dataFlowDirection() == DataFlowDirection.RESPONSE )
                    log.info(advice.className() + " has transformed the response to: " + advice.responseMessage());

            }
        }
        log.info("*** Done reducing the advice list. ***");
    }

    private void updateSolutionAdvice(XformAdviceRecord advice) {

        XformAdvice transformAdvice = new XformAdvice(advice.className(), advice.descriptor());

        if ( advice.className().equals("Hl7Pipeline") && advice.dataFlowDirection() == DataFlowDirection.REQUEST && advice.methodDisposition() == MethodDisposition.PREEXECUTION) {
            // This is the case where we are setting up a new TransformAdvice
            transformAdvice.setRequest(advice.requestMessage());
            xformAdviceMap.put(advice.descriptorId(), transformAdvice);
            return;
        }

        if ( advice.className().equals("Hl7Pipeline") && advice.dataFlowDirection() == DataFlowDirection.RESPONSE && advice.methodDisposition() == MethodDisposition.PREEXECUTION) {
            // This element in the map should already be here.
            XformAdvice existingAdvice = xformAdviceMap.get(advice.descriptorId());
            existingAdvice.setResponse(advice.responseMessage());
            xformAdviceMap.put(advice.descriptorId(), existingAdvice);
            return;
        }

        // Case to set the pipeline transformedRequest



        if ( !xformAdviceMap.containsKey(advice.descriptorId()) ) {
            XformAdvice solutionAdvice = new XformAdvice(advice.className(), advice.descriptor()); // , advice.requestMessage(), null, advice.responseMessage(), null);
            xformAdviceMap.put(advice.descriptorId(), solutionAdvice);
            return;
        }

        XformAdvice existingAdvice = xformAdviceMap.get(advice.descriptorId());

        if ( advice.className().equals("Hl7Pipeline") && advice.dataFlowDirection() == DataFlowDirection.REQUEST && advice.methodDisposition() == MethodDisposition.POSTEXECUTION) {
            existingAdvice.setTransformedRequest(advice.requestMessage());
            xformAdviceMap.put(advice.descriptorId(), existingAdvice);
            return;
        }

        if ( !advice.className().endsWith("Pipe"))
            existingAdvice.setName(advice.descriptor());

        // TODO next thing to debug is here... a solution is being executed, but there are no request operations, but we are treating it as if there was and setting the transformedRequest which is wrong.
        // if ( isTransformed(advice) ) {
        if ( advice.hasTransformed() ) {
            if ( advice.dataFlowDirection() == DataFlowDirection.REQUEST )
                existingAdvice.setTransformedRequest(advice.requestMessage());
            else if ( advice.dataFlowDirection() == DataFlowDirection.RESPONSE )
                existingAdvice.setTransformedResponse(advice.responseMessage());

        }
        /*
            * name: "Zip Fixer"
            * request:
            * transformedRequest:
            * response:
            * transformedResponse:
            *
            * sdsdf
         */
        xformAdviceMap.put(advice.descriptorId(), existingAdvice);
    }

    private boolean isTransformed(XformAdviceRecord advice) {
        if ( advice.className().equals("Solution")) {
            return true;
        }
        return false;
    }

    @Override
    public void logIt() {
        computeTransactionTimes();
        log.info(Markers2.append("transactionData", this), "{}", getMessage());
    }
}
