package gov.cdc.izgateway.transformation.preconditions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
abstract class Exists implements Precondition {
   private String dataPath;

    protected Exists() {}

    protected Exists(Exists exists) {
        this.dataPath = exists.getDataPath();
    }

    protected Exists(String dataPath) {
        this.dataPath = dataPath;
    }
}
