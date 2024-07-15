package gov.cdc.izgateway.transformation.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModelUtils {

    private ModelUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static <T extends BaseModel> List<T> filterList(Boolean includeInactive, List<T> allList) {
        if (Boolean.FALSE.equals(includeInactive)) {
            return allList.stream()
                    .filter(BaseModel::getActive)
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>(allList);
        }
    }

}
