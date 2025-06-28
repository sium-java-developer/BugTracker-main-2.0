package org.bugtracker.entities;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * A Data Transfer Object (DTO) for handling user profile updates.
 * This class carries data from the profile edit form to the controller
 * and includes validation rules.
 */
public class UserProfileUpdateDto {

    @NotBlank(message = "First name cannot be empty")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name cannot be empty")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    // This field is included to be displayed on the form (read-only)
    // and to be available if needed during processing, but it's not typically updated.
    @NotBlank
    private String username;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Please provide a valid email address")
    private String email;

    /**
     * The new password. This field is optional.
     * The pattern allows the string to be either empty OR have at least 8 characters.
     * This prevents validation errors when the user does not wish to change their password.
     */
    @Pattern(regexp = "^$|.{8,}", message = "Password must be at least 8 characters long")
    private String password;

    // --- Getters and Setters ---

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}