package gov.cdc.izgateway.xform.repository.dynamodb.migration;

import gov.cdc.izgateway.xform.model.Event;
import gov.cdc.izgateway.xform.repository.XformRepository;
import gov.cdc.izgateway.xform.repository.dynamodb.DynamoDbRepositoryFactory;
import gov.cdc.izgateway.xform.repository.dynamodb.EventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

@Service
@Slf4j
public class MigrationLockService {

    private final DynamoDbRepositoryFactory repositoryFactory;
    private final XformRepository<Event> eventRepository;

    @Autowired
    public MigrationLockService(DynamoDbRepositoryFactory repositoryFactory) {
        this.repositoryFactory = repositoryFactory;
        this.eventRepository = repositoryFactory.eventRepository();
    }

    public boolean acquireLock() {
        try {
            if (isMigrationInProgress()) {
                log.warn("Migration is already in progress by another node.");
                return false;
            }
            Event event = new Event();
            event.setName("Migration");
            event.setStarted(Instant.now());
            eventRepository.createEntity(event);
            log.info("Migration lock acquired by node: {}", event.getReportedBy());
            return true;
        } catch (Exception e) {
            log.warn("Failed to acquire migration lock, another migration may be in progress: {}", e.getMessage());
            return false;
        }
    }

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

    public boolean isMigrationInProgress() {
        // Loop through all events in the entity set to see if there is an event that is IN_PROGRESS
        for (Event event : eventRepository.getEntitySet()) {
            // If event.completed is null then return true
            if (event.getCompleted() == null) {
                return true;
            }
        }

        return false;
    }

    private Event getActiveEvent() {
        for (Event event : eventRepository.getEntitySet()) {
            if (event.getCompleted() == null) {
                return event;
            }
        }
        return null;
    }
}
