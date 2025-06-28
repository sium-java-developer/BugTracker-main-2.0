package org.bugtracker.util;

import java.util.Arrays;
import java.util.Optional;

/**
 * Represents the specific fields that can be targeted by search criteria.
 * This provides a type-safe way to define searchable attributes.
 */
public enum SearchableBugField {
    TITLE("title"),
    DESCRIPTION("description"), // Example: if you want to search description
    STATUS("status"),
    PRIORITY("priority");
    // Add other searchable fields as needed

    private final String fieldName;

    SearchableBugField(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Gets the actual field name string, which might be used in database queries.
     * @return The string representation of the field name.
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Attempts to find a SearchableBugField enum constant based on a string input.
     * This method is case-insensitive.
     *
     * @param inputField The string representation of the field to find.
     * @return An Optional containing the matching SearchableBugField, or an empty Optional if no match is found or input is null/blank.
     */
    public static Optional<SearchableBugField> fromString(String inputField) {
        if (inputField == null || inputField.isBlank()) {
            return Optional.empty();
        }
        String upperInputField = inputField.toUpperCase();
        return Arrays.stream(values())
                .filter(fieldEnum -> fieldEnum.name().equals(upperInputField) || fieldEnum.getFieldName().equalsIgnoreCase(inputField))
                .findFirst();
    }

    /**
     * A stricter version that only matches the enum constant name (case-insensitive).
     * Throws IllegalArgumentException for invalid input, similar to Enum.valueOf().
     *
     * @param input The string to convert to a SearchableBugField.
     * @return The corresponding SearchableBugField.
     * @throws IllegalArgumentException if the input string does not match any enum constant name.
     * @throws NullPointerException if the input string is null.
     */
    public static SearchableBugField fromNameStrict(String input) {
        if (input == null) {
            throw new NullPointerException("Input field name cannot be null");
        }
        return SearchableBugField.valueOf(input.toUpperCase());
    }
}