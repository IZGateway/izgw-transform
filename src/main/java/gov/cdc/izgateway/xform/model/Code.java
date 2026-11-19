package gov.cdc.izgateway.xform.model;

/**
 * A code and the code system that defines it.
 *
 * @param code the code value
 * @param codeSystem the code system
 */
public record Code(String code, String codeSystem) {
    /**
     * Creates a code.
     *
     * @throws IllegalArgumentException if the code or the code system is null or blank
     */
    public Code {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Code is required");
        }
        if (codeSystem == null || codeSystem.isBlank()) {
            throw new IllegalArgumentException("Code system is required");
        }
    }
}
