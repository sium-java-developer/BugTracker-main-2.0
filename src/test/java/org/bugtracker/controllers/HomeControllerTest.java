package org.bugtracker.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the HomeController.
 * Uses @SpringBootTest to load the full application context and @AutoConfigureMockMvc to configure MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("HomeController Tests")
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("GET / and /home - Home Pages")
    class HomePagesTests {
        @Test
        @DisplayName("Should display the home page for '/'")
        void home_root_ShouldReturnHomePage() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("home"))
                    .andExpect(model().attribute("currentPage", "home"));
        }

        @Test
        @DisplayName("Should display the home page for '/home'")
        void home_path_ShouldReturnHomePage() throws Exception {
            mockMvc.perform(get("/home"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("home"))
                    .andExpect(model().attribute("currentPage", "home"));
        }
    }

    @Nested
    @DisplayName("GET /auth - Login Page")
    class LoginPageTests {
        @Test
        @DisplayName("Should display the login page without error or logout messages")
        void loginPage_NoParams_ShouldReturnLoginPage() throws Exception {
            mockMvc.perform(get("/auth"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/login"))
                    .andExpect(model().attribute("currentPage", "auth"))
                    .andExpect(model().attributeDoesNotExist("loginError"))
                    .andExpect(model().attributeDoesNotExist("logoutMessage"));
        }

        @Test
        @DisplayName("Should display the login page with an error message")
        void loginPage_WithErrorParam_ShouldReturnLoginPageWithError() throws Exception {
            mockMvc.perform(get("/auth").param("error", "true"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/login"))
                    .andExpect(model().attribute("currentPage", "auth"))
                    .andExpect(model().attribute("loginError", "Invalid username or password."))
                    .andExpect(model().attributeDoesNotExist("logoutMessage"));
        }

        @Test
        @DisplayName("Should display the login page with a logout message")
        void loginPage_WithLogoutParam_ShouldReturnLoginPageWithLogoutMessage() throws Exception {
            mockMvc.perform(get("/auth").param("logout", "true"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/login"))
                    .andExpect(model().attribute("currentPage", "auth"))
                    .andExpect(model().attributeDoesNotExist("loginError"))
                    .andExpect(model().attribute("logoutMessage", "You have been logged out successfully."));
        }
    }

    @Nested
    @DisplayName("GET /about - About Page")
    class AboutPageTests {
        @Test
        @DisplayName("Should display the about page")
        void aboutPage_ShouldReturnAboutPage() throws Exception {
            mockMvc.perform(get("/about"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("about"))
                    .andExpect(model().attribute("currentPage", "about"));
        }
    }
}