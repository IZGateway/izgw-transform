package gov.cdc.izgateway.transformation.transformers;

import gov.cdc.izgateway.transformation.configuration.DataTransformationConfig;

abstract class BaseDataTransformation implements DataTransformation {
    DataTransformationConfig configuration;

    protected BaseDataTransformation(DataTransformationConfig dataTransformationConfig) {
        this.configuration = dataTransformationConfig;
    }
}
