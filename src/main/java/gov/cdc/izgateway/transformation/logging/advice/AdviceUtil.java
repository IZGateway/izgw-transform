package gov.cdc.izgateway.transformation.logging.advice;

public class AdviceUtil {
    private AdviceUtil() {
    }
    
    public static boolean isPipelineAdvice(String className) {
        return className.equals("PipelineRunnerService");
    }

    public static boolean isSolutionAdvice(String className) {
        return className.contains("Solution") || className.endsWith("Pipe");
    }

    // TODO - add Precondition

}
