package gov.cdc.izgateway.transformation.logging.advice;

public class AdviceUtil {
    public static boolean isPipelineAdvice(String className) {
        return className.equals("Hl7Pipeline");
    }

    public static boolean isSolutionAdvice(String className) {
        return className.contains("Solution") || className.endsWith("Pipe");
    }

    public static boolean isOperationAdvice(String className) {
        return className.endsWith("Operation");
    }

}
