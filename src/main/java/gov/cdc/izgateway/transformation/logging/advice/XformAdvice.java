package gov.cdc.izgateway.transformation.logging.advice;

import lombok.Data;

import java.util.ArrayList;

@Data
public class XformAdvice {

    public XformAdvice() {
    }

    public XformAdvice(String className, String name) {
        this.className = className;
        this.name = name;
    }

    /*
     * name: "Zip Fixer"
     * request:
     * transformedRequest:
     * response:
     * transformedResponse:
     *
     * sdsd
     */

    private String className;
    private String name;
    private String request;
    private String transformedRequest;
    private String response;
    private String transformedResponse;

    private ArrayList<XformAdvice> children = new ArrayList<>();
    private ArrayList<XformAdvice> siblings = new ArrayList<>();

    public void addChild(XformAdvice child) {
        children.add(child);
    }

    public void addSibling(XformAdvice sibling) {
        siblings.add(sibling);
    }

}
