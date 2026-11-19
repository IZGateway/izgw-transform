package gov.cdc.izgateway.xform.logging.advice;

/**
 * Classifies advised classes by their simple class name.
 */
public class AdviceUtil {
    private AdviceUtil() {
    }
    
    /**
     * Returns true if the class is the pipeline runner.
     *
     * @param className the simple class name
     * @return true if the class name is PipelineRunnerService
     */
    public static boolean isPipelineAdvice(String className) {
        return className.equals("PipelineRunnerService");
    }

    /**
     * Returns true if the class is a solution, a solution operation or a pipe.
     *
     * @param className the simple class name
     * @return true if the class name contains "Solution" or ends with "Pipe"
     */
    public static boolean isSolutionAdvice(String className) {
        return className.contains("Solution") || className.endsWith("Pipe");
    }

}
