package org.bugtracker.controllers;

import org.bugtracker.entities.User;
import org.bugtracker.problems.UserAlreadyExistsException;
import org.bugtracker.services.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the AuthController.
 * Uses @SpringBootTest to load the full application context and @AutoConfigureMockMvc to configure MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Nested
    @DisplayName("GET /register")
    class ShowRegistrationForm {

        @Test
        @DisplayName("Should display the registration form successfully")
        void showRegistrationForm_Success() throws Exception {
            mockMvc.perform(get("/register"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/register"))
                    .andExpect(model().attributeExists("user"))
                    .andExpect(model().attribute("currentPage", "register"));
        }
    }

    @Nested
    @DisplayName("POST /register")
    class RegisterUser {

        @Test
        @DisplayName("Should register a user successfully and redirect to login")
        void registerUser_Success() throws Exception {
            // Arrange: Assume the service layer handles the registration without error
            // Note: registerUser returns a User, but the controller doesn't use the return value.
            when(userService.registerUser(any(User.class))).thenReturn(new User());

            // Act & Assert
            mockMvc.perform(post("/register")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("username", "testuser")
                            .param("password", "password123")
                            .param("firstName", "Test")
                            .param("lastName", "User")
                            .param("email", "test@example.com")
                            .with(csrf())) // Include CSRF token for POST requests
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/auth"))
                    .andExpect(flash().attribute("successMessage", "Registration successful! Please log in."));

            // Verify that the service method was called exactly once
            verify(userService, times(1)).registerUser(any(User.class));
        }

        @Test
        @DisplayName("Should return to form with errors if validation fails")
        void registerUser_ValidationErrors() throws Exception {
            // Act & Assert
            mockMvc.perform(post("/register")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("username", "") // Invalid: empty username
                            .param("password", "short") // Invalid: too short
                            .param("email", "invalid-email") // Invalid: not a valid email format
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/register"))
                    .andExpect(model().attributeHasFieldErrors("user", "username", "password", "email"))
                    .andExpect(model().attribute("currentPage", "register"));

            // Verify that the service method was never called due to validation failure
            verify(userService, never()).registerUser(any(User.class));
        }

        @Test
        @DisplayName("Should return to form with error if username already exists")
        void registerUser_UserAlreadyExists() throws Exception {
            // Arrange: Configure the mock service to throw the specific exception
            String exceptionMessage = "Username 'existinguser' is already taken.";
            doThrow(new UserAlreadyExistsException(exceptionMessage))
                    .when(userService).registerUser(any(User.class));

            // Act & Assert
            mockMvc.perform(post("/register")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("username", "existinguser")
                            .param("password", "password123")
                            .param("firstName", "Existing")
                            .param("lastName", "User")
                            .param("email", "existing@example.com")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/register"))
                    .andExpect(model().attribute("registrationError", exceptionMessage))
                    .andExpect(model().attribute("currentPage", "register"));

            // Verify the service method was still called
            verify(userService, times(1)).registerUser(any(User.class));
        }

        @Test
        @DisplayName("Should return to form with generic error if an unexpected exception occurs")
        void registerUser_GenericError() throws Exception {
            // Arrange: Configure the mock service to throw a generic runtime exception
            doThrow(new RuntimeException("Database connection failed."))
                    .when(userService).registerUser(any(User.class));

            // Act & Assert
            mockMvc.perform(post("/register")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("username", "someuser")
                            .param("password", "password123")
                            .param("firstName", "Some")
                            .param("lastName", "User")
                            .param("email", "some@example.com")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/register"))
                    .andExpect(model().attribute("registrationError", "An unexpected error occurred during registration. Please try again."))
                    .andExpect(model().attribute("currentPage", "register"));

            // Verify the service method was called
            verify(userService, times(1)).registerUser(any(User.class));
        }
    }
}