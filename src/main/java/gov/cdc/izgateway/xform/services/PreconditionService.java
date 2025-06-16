package gov.cdc.izgateway.xform.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import gov.cdc.izgateway.xform.logging.ApiEventLogger;
import gov.cdc.izgateway.xform.model.PreconditionInfo;
import gov.cdc.izgateway.xform.model.PreconditionInfoProperty;
import gov.cdc.izgateway.xform.preconditions.Precondition;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

@Service
public class PreconditionService implements XformService<PreconditionInfo> {
    @Override
    public PreconditionInfo getObject(UUID id) {
        return null;
    }

    @Override
    public List<PreconditionInfo> getList() {

        // Use reflection to determine the preconditions that exist in the running system
        // Start with pulling the annotation on the Precondition interface
        // Then build up array of PreconditionInformation to show fields necessary
        // when configuring each precondition.
        Class<?> preconditionClass = Precondition.class;
        JsonSubTypes subTypesAnnotation = preconditionClass.getAnnotation(JsonSubTypes.class);
        List<PreconditionInfo> preconditionInfoList = new ArrayList<>();

        if (subTypesAnnotation != null) {
            JsonSubTypes.Type[] types = subTypesAnnotation.value();

            for (JsonSubTypes.Type type : types) {
                Class<?> subClass = type.value();
                PreconditionInfo info = new PreconditionInfo();
                info.setMethod(type.name());

                Map<String, PreconditionInfoProperty> properties = new HashMap<>();
                getAllFields(subClass, properties);
                info.setProperties(properties);

                preconditionInfoList.add(info);
            }
        }

        ApiEventLogger.logReadEvent(preconditionInfoList);
        return preconditionInfoList;
    }

    @Override
    public void update(PreconditionInfo obj) {
        // Preconditions are determined and listed via code reflection, not to be updated in code.
    }

    @Override
    public void create(PreconditionInfo obj) {
        // Preconditions are determined and listed via code reflection, not to be created in code.
    }

    @Override
    public void delete(UUID id) {
        // Preconditions are determined and listed via code reflection, not to be deleted in code.
    }

    private boolean shouldIncludeField(Field field) {
        // If a field is annotated with @JsonIgnore in a Precondition do not include this in the output.
        // If a field is static do not include in the output.  Only non-static fields would be updated in
        // the state so this will exclude the AspectJ objects that may show up.
        return !field.isAnnotationPresent(JsonIgnore.class) && !Modifier.isStatic(field.getModifiers());
    }

    private void getAllFields(Class<?> clazz, Map<String, PreconditionInfoProperty> properties) {
        if (clazz == null || clazz == Object.class) {
            return;
        }

        // Process current class
        for (Field field : clazz.getDeclaredFields()) {
            if (shouldIncludeField(field)) {
                PreconditionInfoProperty prop = new PreconditionInfoProperty();
                prop.setType(field.getType().getSimpleName());
                prop.setRequired(!field.isAnnotationPresent(Nullable.class));
                properties.put(field.getName(), prop);
            }
        }

        // Process superclass
        getAllFields(clazz.getSuperclass(), properties);
    }
}
