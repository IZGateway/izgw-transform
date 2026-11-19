package gov.cdc.izgateway.xform.logging.advice;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.UUID;

/**
 * The Advisable interface defines the contract for objects to be logged
 * within the IZG Xform Service.
 *
 * <p>Implementing classes need to provide methods for retrieving a name and a
 * unique identifier for the advisable object.
 *
 * <p>Please <em>note</em> that the Name is set to ignore because, at this time because
 * of how the available Operation and Precondition lists via the API are
 * dynamically generated via reflection.  Without this the name was coming
 * out in API calls when it is not a field that exists in the configuration
 * JSON itself.
 *
 * @since 0.2.0
 */
public interface Advisable {
    /**
     * Returns the name of the advisable object that is recorded in the advice.
     *
     * @return the name
     */
    @JsonIgnore
    String getName();

    /**
     * Returns the unique identifier of the advisable object that is recorded in the advice.
     *
     * @return the identifier
     */
    UUID getId();
}
