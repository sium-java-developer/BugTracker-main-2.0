package org.bugtracker.problems;

import java.io.Serial;

/**
 * Exception thrown when an attempt is made to create a user that already exists
 * (e.g., with the same username or email).
 */
public class UserAlreadyExistsException extends RuntimeException { // Changed to RuntimeException

    @Serial
    private static final long serialVersionUID = 1L; // Added serialVersionUID

    /**
     * Constructs a new UserAlreadyExistsException with the specified detail message.
     *
     * @param message The detail message.
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }

    /**
     * Constructs a new UserAlreadyExistsException with the specified detail message and cause.
     *
     * @param message The detail message.
     * @param cause   The cause (which is saved for later retrieval by the
     *                {@link #getCause()} method). (A {@code null} value is
     *                permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}