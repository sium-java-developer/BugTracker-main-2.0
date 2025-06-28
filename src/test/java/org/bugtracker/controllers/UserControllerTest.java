package org.bugtracker.controllers;

import org.bugtracker.entities.User;
import org.bugtracker.problems.NotFoundException;
import org.bugtracker.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DisplayName("UserController Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmail("test@example.com");
        testUser.setAdmin(false);

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setEmail("admin@example.com");
        adminUser.setAdmin(true);
    }

    // --- User Profile Tests ---

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("GET /profile - Should display current user profile")
    void getCurrentUserProfile_Success() throws Exception {
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/users/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/profile"))
                .andExpect(model().attribute("user", hasProperty("username", is("testuser"))))
                .andExpect(model().attribute("currentPage", "profile"));
    }

    // @Test
    // @WithMockUser(username = "testuser")
    // @DisplayName("GET /profile/edit - Should display edit form for current user")
    // void showEditProfileForm_Success() throws Exception {
    //     when(userService.findByUsername("testuser")).thenReturn(Optional.of(testUser));

    //     mockMvc.perform(get("/users/profile/edit"))
    //             .andExpect(status().isOk())
    //             .andExpect(view().name("users/profile-edit"))
    //             .andExpect(model().attribute("userDto", allOf(
    //                     hasProperty("username", is("testuser")),
    //                     hasProperty("firstName", is("Test"))
    //             )));
    // }

    // @Test
    // @WithMockUser(username = "testuser")
    // @DisplayName("POST /profile/update - Should update profile successfully")
    // void updateCurrentUserProfile_Success() throws Exception {
    //     mockMvc.perform(post("/users/profile/update").with(csrf())
    //                     .contentType(MediaType.APPLICATION_FORM_URLENCODED)
    //                     .param("firstName", "UpdatedFirst")
    //                     .param("lastName", "UpdatedLast")
    //                     .param("email", "updated@example.com")
    //                     .param("password", "newPassword123"))
    //             .andExpect(status().is3xxRedirection())
    //             .andExpect(redirectedUrl("/users/profile"))
    //             .andExpect(flash().attribute("successMessage", "Profile updated successfully."));

    //     verify(userService).updateUserProfile(eq("testuser"), any(UserProfileUpdateDto.class));
    // }

    // @Test
    // @WithMockUser(username = "testuser")
    // @DisplayName("POST /profile/update - Should fail on validation error and repopulate username")
    // void updateCurrentUserProfile_ValidationFailure() throws Exception {
    //     mockMvc.perform(post("/users/profile/update").with(csrf())
    //                     .contentType(MediaType.APPLICATION_FORM_URLENCODED)
    //                     .param("firstName", "") // Invalid: first name is blank
    //                     .param("lastName", "User")
    //                     .param("email", "test@example.com"))
    //             .andExpect(status().isOk())
    //             .andExpect(view().name("users/profile-edit"))
    //             .andExpect(model().hasErrors())
    //             .andExpect(model().attributeHasFieldErrors("userDto", "firstName"))
    //             // Verify the username is repopulated on the DTO
    //             .andExpect(model().attribute("userDto", hasProperty("username", is("testuser"))));
    // }

    // --- Admin User Management Tests ---

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /users - Admin should see list of users")
    void listUsers_AsAdmin_Success() throws Exception {
        Page<User> userPage = new PageImpl<>(Collections.singletonList(testUser));
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/list"))
                .andExpect(model().attribute("usersPage", hasProperty("totalElements", is(1L))))
                .andExpect(model().attribute("currentPage", "users"));
    }

    @Test
    @WithMockUser(roles = "USER") // Note: Spring Security automatically adds ROLE_ prefix
    @DisplayName("GET /users - Regular user should be forbidden")
    void listUsers_AsUser_Forbidden() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /users/new - Admin should see create user form")
    void getCreateUserPage_AsAdmin_Success() throws Exception {
        mockMvc.perform(get("/users/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/create"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /users - Admin should create user successfully")
    void createUser_AsAdmin_Success() throws Exception {
        mockMvc.perform(post("/users").with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "newbie")
                        .param("firstName", "New")
                        .param("lastName", "Bie")
                        .param("email", "newbie@example.com")
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"))
                .andExpect(flash().attribute("successMessage", "User 'newbie' created successfully."));

        verify(userService).createUser(any(User.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /users/{id}/edit - Admin should update user successfully")
    void updateUser_AsAdmin_Success() throws Exception {
        mockMvc.perform(post("/users/1/edit").with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "testuser")
                        .param("firstName", "AdminUpdated")
                        .param("lastName", "User")
                        .param("email", "test@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));

        verify(userService).updateUser(any(User.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /users/{id} - Admin should delete user successfully")
    void deleteUser_AsAdmin_Success() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/users/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"))
                .andExpect(flash().attribute("successMessage", "User deleted successfully."));

        verify(userService).deleteUser(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /users/{id} - Should handle non-existent user on delete")
    void deleteUser_AsAdmin_NotFound() throws Exception {
        doThrow(new NotFoundException("User not found")).when(userService).deleteUser(99L);

        mockMvc.perform(delete("/users/99").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(userService).deleteUser(99L);
    }
}