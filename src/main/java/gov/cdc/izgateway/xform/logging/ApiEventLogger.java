package gov.cdc.izgateway.xform.logging;

import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.logging.markers.Markers2;
import gov.cdc.izgateway.xform.model.BaseModel;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Logs configuration API events (read, create, update, delete) with an "apiLog" marker, unless API logging is disabled.
 */
@Slf4j
public class ApiEventLogger {

    /**
     * Logs a READ event for one entity.
     *
     * @param <T> the entity type
     * @param data the entity that was read
     */
    public static <T> void logReadEvent(T data) {
        logApiEvent("READ", getClassName(data), getId(data),  null, null);
    }

    /**
     * Logs a READ event for a list of entities. Nothing is logged if the list is null or empty.
     *
     * @param <T> the entity type
     * @param data the entities that were read
     */
    public static <T> void logReadEvent(List<T> data) {
        if (data != null && !data.isEmpty()) {
            logApiEvent("READ", getClassName(data.get(0)), null, null, null);
        }
    }

    /**
     * Logs a DELETE event, with the deleted entity as the old data.
     *
     * @param <T> the entity type
     * @param dataToDelete the entity to delete
     */
    public static <T> void logDeleteEvent(T dataToDelete) {
        logApiEvent("DELETE", getClassName(dataToDelete), getId(dataToDelete), null, dataToDelete);
    }

    /**
     * Logs a CREATE event, with the created entity as the new data.
     *
     * @param <T> the entity type
     * @param newData the entity that was created
     */
    public static <T> void logCreateEvent(T newData) {
        logApiEvent("CREATE", getClassName(newData), getId(newData), newData, null);
    }

    /**
     * Logs an UPDATE event with the new and the old data.
     *
     * @param <T> the entity type
     * @param newData the entity after the update
     * @param existingData the entity before the update
     */
    public static <T> void logUpdateEvent(T newData, T existingData) {
        logApiEvent("UPDATE", getClassName(newData), getId(newData), newData, existingData);
    }

    private static <T> void logApiEvent(String eventDescription, String objectName, String objectId, T newData, T existingData) {
        if ( XformRequestContext.isApiLoggingDisabled() ) {
            return;
        }

        XformApiDetail data = new XformApiDetail();
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

    private ApiEventLogger() {
    }
}