package gov.cdc.izgateway.xform.logging.advice;

public class AdviceUtil {
    private AdviceUtil() {
    }
    
    public static boolean isPipelineAdvice(String className) {
        return className.equals("PipelineRunnerService");
    }

    public static boolean isSolutionAdvice(String className) {
        return className.contains("Solution") || className.endsWith("Pipe");
    }

}
