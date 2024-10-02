package gov.cdc.izgateway.transformation.util;

import java.util.function.Function;

public class Utils {
    public static <T, R> boolean isEmpty(T obj, Function<T, R> mapper) {
        if (obj == null) {
            return true;
        }
        R result = mapper.apply(obj);
        if (result == null) {
            return true;
        }

        return result instanceof Object[] resultArray && resultArray.length == 0;
    }
}