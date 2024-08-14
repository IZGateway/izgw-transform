package gov.cdc.izgateway.transformation.model;

public record Code(String code, String codeSystem) {
    public Code {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Code is required");
        }
        if (codeSystem == null || codeSystem.isBlank()) {
            throw new IllegalArgumentException("Code system is required");
        }
    }
}
