package gov.cdc.izgateway.transformation.logging.advice;

import com.fasterxml.jackson.annotation.JsonInclude;
import gov.cdc.izgateway.logging.event.TransactionData;
import gov.cdc.izgateway.logging.markers.MarkerObjectFieldName;
import gov.cdc.izgateway.logging.markers.Markers2;
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
    private ArrayList<XformAdvice> xformAdviceList = new ArrayList<>();

    // TODO - Paul to add logic to change addAdvice to create a structure that we documented in the PowerPoint template
    private Map<String, XformAdvice> elements = new LinkedHashMap<>();


    public XformTransactionData() {
        super();
    }

    public XformTransactionData(String eventId) {
        super(eventId);
    }

    public void addAdvice(XformAdvice advice) {
        xformAdviceList.add(advice);
    }

    @Override
    public void logIt() {
        computeTransactionTimes();
        log.info(Markers2.append("transactionData", this), "{}", getMessage());
    }
}
///////
//import java.util.LinkedHashMap;
//import java.util.Map;
//
//public class ElementManager {
//    private Map<String, Element> elements = new LinkedHashMap<>();
//
//    public void addElement(Element element) {
//        elements.put(element.getId(), element);
//    }
//
//    public Element findElementById(String id) {
//        return elements.get(id);
//    }
//
//    public void updateElement(String id, Element newElement) {
//        if (elements.containsKey(id)) {
//            elements.put(id, newElement);
//        }
//    }
//
//    // Additional methods as needed
//}