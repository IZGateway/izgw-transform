package gov.cdc.izgateway.transformation.solutions;

import gov.cdc.izgateway.transformation.configuration.SolutionConfig;
import lombok.extern.java.Log;

@Log
public class Solution {
    private final SolutionConfig configuration;

    public Solution(SolutionConfig configuration) {
        this.configuration = configuration;
    }
}
