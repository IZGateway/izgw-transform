package gov.cdc.izgateway.xform.repository.dynamodb.migration;

import gov.cdc.izgateway.xform.model.Event;
import gov.cdc.izgateway.xform.repository.XformRepository;
import gov.cdc.izgateway.xform.repository.dynamodb.DynamoDbRepositoryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Instant;

/**
 * Service for managing a migration lock using DynamoDB events.
 * Ensures that only one migration process can run at a time across distributed nodes.
 */
@Service
@Slf4j
public class MigrationLockService {

    private final XformRepository<Event> eventRepository;

    @Autowired
    public MigrationLockService(DynamoDbRepositoryFactory repositoryFactory) {
        this.eventRepository = repositoryFactory.eventRepository();
    }

    /**
     * Attempts to acquire the migration lock.
     * @return true if the lock was acquired, false if another migration is in progress.
     */
    public boolean acquireLock() {
        try {
            if (isMigrationInProgress()) {
                log.warn("Migration is already in progress by another node.");
                return false;
            }

            Event event = new Event();
            event.setName(Event.MIGRATION);
            event.setStarted(Instant.now());
            eventRepository.createEntity(event);
            log.info("Migration lock acquired by node: {}", event.getReportedBy());

            return true;
        } catch (Exception e) {
            log.warn("Failed to acquire migration lock, another migration may be in progress: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Releases the migration lock by marking the active event as completed.
     */
    public void releaseLock() {
        try {
            Event event = getActiveEvent();
            if (event == null) {
                log.warn("No active migration event found to release lock.");
                return;
            }
            event.setCompleted(Instant.now());
            eventRepository.updateEntity(event);
            log.info("Migration lock released by node: {}", event.getReportedBy());
        } catch (Exception e) {
            log.error("Failed to release migration lock: {}", e.getMessage(), e);
        }
    }

    /**
     * Checks if a migration is currently in progress.
     * @return true if a migration event is active and not completed, false otherwise.
     */
    public boolean isMigrationInProgress() {
        // Loop through all events in the entity set to see if there is an event that is in progress
        for (Event event : eventRepository.getEntitySet()) {
            // If event.completed is null then return true
            if (event.getCompleted() == null) {
                return true;
            }
        }

        return false;
    }

    /**
     * Retrieves the currently active migration event, if any.
     * @return the active Event, or null if none is active.
     */
    private Event getActiveEvent() {
        for (Event event : eventRepository.getEntitySet()) {
            if (event.getCompleted() == null) {
                return event;
            }
        }
        return null;
    }
}