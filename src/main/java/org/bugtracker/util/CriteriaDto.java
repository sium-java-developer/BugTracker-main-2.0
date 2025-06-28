package org.bugtracker.util;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull; // For the enum field

import java.util.Objects;

public class CriteriaDto {

    @NotNull // Field name is crucial for criteria
    private SearchableBugField field; // Changed from String to the enum for type safety

    @NotEmpty(message = "Search value cannot be empty")
    private String value; // Renamed from fieldValue for brevity

    private boolean exactMatch = false; // Default to 'contains' search (exactMatch = false)

    /**
     * Field intended to communicate a message if no results are found.
     * Note: This is an unconventional use for an input DTO.
     * Consider handling "no results" messages in the response or UI layer.
     */
    private String noResultsMessage; // Renamed for clarity

    // Constructors (optional, but can be useful)
    public CriteriaDto() {
    }

    public CriteriaDto(SearchableBugField field, String value) {
        this.field = field;
        this.value = value;
    }

    public CriteriaDto(SearchableBugField field, String value, boolean exactMatch) {
        this.field = field;
        this.value = value;
        this.exactMatch = exactMatch;
    }

    // Getters and Setters
    public SearchableBugField getField() {
        return field;
    }

    public void setField(SearchableBugField field) {
        this.field = field;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isExactMatch() { // Changed getter name for boolean
        return exactMatch;
    }

    public void setExactMatch(boolean exactMatch) {
        this.exactMatch = exactMatch;
    }

    public String getNoResultsMessage() {
        return noResultsMessage;
    }

    public void setNoResultsMessage(String noResultsMessage) {
        this.noResultsMessage = noResultsMessage;
    }

    /**
     * Checks if the essential criteria fields (field and value) are effectively empty or not set.
     *
     * @return true if the criteria is considered empty, false otherwise.
     */
    public boolean isEmpty() {
        return field == null || value == null || value.isBlank();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CriteriaDto that = (CriteriaDto) o;
        return exactMatch == that.exactMatch &&
                field == that.field &&
                Objects.equals(value, that.value) &&
                Objects.equals(noResultsMessage, that.noResultsMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, value, exactMatch, noResultsMessage);
    }

    @Override
    public String toString() {
        return "CriteriaDto{" +
                "field=" + field +
                ", value='" + value + '\'' +
                ", exactMatch=" + exactMatch +
                ", noResultsMessage='" + noResultsMessage + '\'' +
                '}';
    }
}