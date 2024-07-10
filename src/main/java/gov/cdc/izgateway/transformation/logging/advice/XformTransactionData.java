package gov.cdc.izgateway.transformation.logging.advice;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import gov.cdc.izgateway.logging.event.TransactionData;
import gov.cdc.izgateway.logging.markers.MarkerObjectFieldName;
import gov.cdc.izgateway.logging.markers.Markers2;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

@Slf4j
@Data
@MarkerObjectFieldName("transactionData")
@JsonInclude(JsonInclude.Include.ALWAYS)
public class XformTransactionData extends TransactionData {

    // TODO Add @JsonIgnore as we don't with these to be wrriten to the log!!! PHI!
    private ArrayList<XformAdvice> adviceList = new ArrayList<>();

    public XformTransactionData() {
        super();
    }

    public XformTransactionData(String eventId) {
        super(eventId);
    }

    public void addAdvice(XformAdvice advice) {
        adviceList.add(advice);
    }

    @Override
    public void logIt() {
        computeTransactionTimes();
        log.info(Markers2.append("transactionData", this), "{}", getMessage());
    }
}
