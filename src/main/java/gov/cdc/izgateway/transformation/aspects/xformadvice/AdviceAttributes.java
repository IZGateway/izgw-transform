package gov.cdc.izgateway.transformation.aspects.xformadvice;

import lombok.Data;

@Data
public class AdviceAttributes {
    String descriptor;
    String descriptorId;
    boolean hasTransformed;
    String request;
    String response;

    public AdviceAttributes(String descriptor, String descriptorId, boolean hasTransformed, String request, String response) {
        this.descriptor = descriptor;
        this.descriptorId = descriptorId;
        this.hasTransformed = hasTransformed;
        this.request = request;
        this.response = response;
    }
}