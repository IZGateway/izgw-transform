package gov.cdc.izgateway.xform.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import gov.cdc.izgateway.xform.logging.ApiEventLogger;
import gov.cdc.izgateway.xform.model.OperationInfo;
import gov.cdc.izgateway.xform.model.OperationInfoProperty;
import gov.cdc.izgateway.xform.operations.Operation;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

@Service
public class OperationService implements XformService<OperationInfo> {
    @Override
    public OperationInfo getObject(UUID id) {
        return null;
    }

    @Override
    public List<OperationInfo> getList() {
        // Use reflection to determine the operations that exist in the running system
        // Start with pulling the annotation on the Operation interface
        // Then build up array of OperationInfo to show fields necessary
        // when configuring each operation.
        Class<?> operationClass = Operation.class;
        JsonSubTypes subTypesAnnotation = operationClass.getAnnotation(JsonSubTypes.class);
        List<OperationInfo> operationInfoList = new ArrayList<>();

        if (subTypesAnnotation != null) {
            JsonSubTypes.Type[] types = subTypesAnnotation.value();

            for (JsonSubTypes.Type type : types) {
                Class<?> subClass = type.value();
                OperationInfo info = new OperationInfo();
                info.setMethod(type.name());

                Map<String, OperationInfoProperty> properties = new HashMap<>();
                getAllFields(subClass, properties);
                info.setProperties(properties);

                operationInfoList.add(info);
            }
        }
        ApiEventLogger.logReadEvent(operationInfoList);
        return operationInfoList;
    }

    @Override
    public void update(OperationInfo obj) {
        // Operations are determined and listed via code reflection, not to be updated in code.
    }

    @Override
    public void create(OperationInfo obj) {
        // Operations are determined and listed via code reflection, not to be created in code.
    }

    @Override
    public void delete(UUID id) {
        // Operations are determined and listed via code reflection, not to be deleted in code.
    }

    private void getAllFields(Class<?> clazz, Map<String, OperationInfoProperty> properties) {
        if (clazz == null || clazz == Object.class) {
            return;
        }

        // Process current class
        for (Field field : clazz.getDeclaredFields()) {
            if (shouldIncludeField(field)) {
                OperationInfoProperty prop = new OperationInfoProperty();
                prop.setType(field.getType().getSimpleName());
                prop.setRequired(!field.isAnnotationPresent(Nullable.class));
                properties.put(field.getName(), prop);
            }
        }

        // Process superclass
        getAllFields(clazz.getSuperclass(), properties);
    }

    private boolean shouldIncludeField(Field field) {
        // If a field is annotated with @JsonIgnore in an Operation do not include this in the output.
        // If a field is static do not include in the output.  Only non-static fields would be updated in
        // the state so this will exclude the AspectJ objects that may show up.
        return !field.isAnnotationPresent(JsonIgnore.class) && !Modifier.isStatic(field.getModifiers());
    }
}
