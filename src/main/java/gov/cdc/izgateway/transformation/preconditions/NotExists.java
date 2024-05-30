package gov.cdc.izgateway.transformation.preconditions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
abstract class NotExists extends Exists implements Precondition {
    protected NotExists() {}

    protected NotExists(NotExists notExists) {
        super(notExists);
    }

    protected NotExists(String dataPath) {
        super(dataPath);
    }
}
