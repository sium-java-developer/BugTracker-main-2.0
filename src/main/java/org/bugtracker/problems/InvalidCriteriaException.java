package org.bugtracker.problems;

import java.io.Serial;

/**
 * Exception thrown when an operation receives invalid criteria.
 * This typically indicates a problem with input data or business rule validation.
 * Uses a messageKey for internationalization.
 */
public class InvalidCriteriaException extends RuntimeException { // Changed to RuntimeException

    @Serial
    private static final long serialVersionUID = 1L; // Added serialVersionUID

    private final String fieldName;
    private final String messageKey;

    /**
     * Constructs a new InvalidCriteriaException.
     *
     * @param fieldName  The name of the field that caused the validation to fail.
     * @param messageKey The internationalization key for the error message.
     */
    public InvalidCriteriaException(String fieldName, String messageKey) {
        // We could pass a default message to super() if desired,
        // but the primary mechanism here is the messageKey.
        // super("Invalid criteria for field: " + fieldName + " (i18n key: " + messageKey + ")");
        this.fieldName = fieldName;
        this.messageKey = messageKey;
    }

    /**
     * Constructs a new InvalidCriteriaException with a specified cause.
     *
     * @param fieldName  The name of the field that caused the validation to fail.
     * @param messageKey The internationalization key for the error message.
     * @param cause      The underlying cause of this exception.
     */
    public InvalidCriteriaException(String fieldName, String messageKey, Throwable cause) {
        super(cause); // Pass the cause to the superclass
        this.fieldName = fieldName;
        this.messageKey = messageKey;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getMessageKey() {
        return messageKey;
    }
}