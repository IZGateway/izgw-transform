package gov.cdc.izgateway.xform.logging;

import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.logging.markers.Markers2;
import gov.cdc.izgateway.xform.model.BaseModel;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class CrudEventLogger {

    public static <T> void logReadEvent(T data) {
        logCrudEvent("READ", getClassName(data), getId(data),  null, null);
    }

    public static <T> void logReadEvent(List<T> data) {
        if (data != null && !data.isEmpty()) {
            logCrudEvent("READ", getClassName(data.get(0)), null, null, null);
        }
    }

    public static <T> void logDeleteEvent(T dataToDelete) {
        logCrudEvent("DELETE", getClassName(dataToDelete), getId(dataToDelete), null, dataToDelete);
    }

    public static <T> void logCreateEvent(T newData) {
        logCrudEvent("CREATE", getClassName(newData), getId(newData), newData, null);
    }

    public static <T> void logUpdateEvent(T newData, T existingData) {
        logCrudEvent("UPDATE", getClassName(newData), getId(newData), newData, existingData);
    }

    private static <T> void logCrudEvent(String eventDescription, String objectName, String objectId, T newData, T existingData) {
        if ( XformRequestContext.isCrudLoggingDisabled() ) {
            return;
        }

        XformApiCrudDetail data = new XformApiCrudDetail();
        data.setEventId(RequestContext.getEventId());
        data.setOldData(existingData);
        data.setNewData(newData);
        data.setMethod(eventDescription);
        data.setUserName(RequestContext.getPrincipal().getName());
        data.setPrincipalType(RequestContext.getPrincipal().getClass().getSimpleName());
        data.setObject(objectName);
        data.setObjectId(objectId);

        log.info(Markers2.append("apiLog", data), "Log message for {} event on object: {}", eventDescription, objectName);
    }

    private static <T> String getClassName(T data) {
        return data == null ? null : data.getClass().getSimpleName();
    }

    private static <T> String getId(T data) {
        return data == null ? null: ((BaseModel)data).getId().toString();
    }

    private CrudEventLogger() {
    }
}