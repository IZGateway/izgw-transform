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
	public static final String MIGRATION = "Migration";

    private String name;
    private Instant started = Instant.now();
    private Instant completed;
    private String reportedBy = SystemUtils.getHostname();
    private UUID id = UUID.randomUUID();
    private Boolean active =  true;
    
    public Event() {
    }
}
