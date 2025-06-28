package org.bugtracker.problems;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

/**
 * Exception thrown when a requested resource or item cannot be found.
 * Annotated with @ResponseStatus to automatically return an HTTP 404 status
 * when thrown from a Spring MVC controller and not otherwise handled.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND) // Reason can be omitted if message is descriptive
public class NotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L; // Good practice

    // private Long objIdentifier; // Removed as it was unused

    /**
     * Constructs a new NotFoundException with a message derived from the class and ID.
     *
     * @param cls The class of the object that was not found.
     * @param id  The ID of the object that was not found.
     * @param <T> The type of the class.
     */
    public <T> NotFoundException(Class<T> cls, Long id) {
        super(cls.getSimpleName() + " with id: " + id + " does not exist!");
    }

    /**
     * Constructs a new NotFoundException with a custom message.
     *
     * @param message The detail message.
     */
    public NotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new NotFoundException with a custom message and a cause.
     *
     * @param message The detail message.
     * @param cause   The underlying cause of this exception.
     */
    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    // Getter for objIdentifier removed as the field was removed.
    // public Long getObjIdentifier() {
    //     return objIdentifier;
    // }
}