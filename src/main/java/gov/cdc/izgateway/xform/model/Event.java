package gov.cdc.izgateway.xform.model;

import gov.cdc.izgateway.model.MappableEntity;
import gov.cdc.izgateway.utils.SystemUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;


/**
 * Supports Event Tracking
 *
 * @author Audacious Inquiry
 */

@Getter
@Setter
@DynamoDbBean
public class Event extends BaseModel {
	// Well known events.
	/** The Migration event */
	public static final String MIGRATION = "Migration";
	/** The Startup event */
	public static final String STARTUP = "Startup";
	/** The Shutdown event */
	public static final String SHUTDOWN = "Shutdown";
	/** The Global Table Creation event */
	public static final String CREATED = "Created";
	
	/**
	 * Provides easy access to the Map class for Swagger documentation
	 */
	public static class Map extends MappableEntity<Event>{}

    private String name;

    private Instant started = Instant.now();
    
    private Instant completed;
    
    private String reportedBy = SystemUtils.getHostname();
    
    private UUID id = UUID.randomUUID();

    private Boolean active =  true;
    
    public Event() {
    }
    

//	@Override
//	public String getPrimaryId() {
//		if (StringUtils.isEmpty(name)) {
//			throw new IllegalArgumentException("Event name cannot be null or empty");
//		}
//		if (started == null) {
//			started = new Date();
//		}
//		return name + "#" + StringUtils.defaultString(target) + "#" + DateConverter.convert(started);
//	}
	
    /**
     * Get the start time of the event as a date
     * @return	The start time of the event
     */
    public Instant getStarted() {
    	return started;
    }
    
    /**
     * Get the completion time of the event as a date
     * @return	The completion time of the event
     */
    public Instant getCompleted() {
    	return completed;
    }
}
