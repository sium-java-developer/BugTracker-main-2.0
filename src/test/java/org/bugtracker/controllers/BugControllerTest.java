package org.bugtracker.controllers;

import org.bugtracker.entities.Bug;
import org.bugtracker.entities.Priority;
import org.bugtracker.entities.Status;
import org.bugtracker.entities.User;
import org.bugtracker.problems.NotFoundException;
import org.bugtracker.services.BugService;
import org.bugtracker.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("BugController Tests")
class BugControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BugService bugService;

    @MockBean // Mock UserService as it's a dependency for create/edit forms
    private UserService userService;

    private Bug testBug;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testBug = new Bug();
        testBug.setId(1L);
        testBug.setTitle("Test Bug Title");
        testBug.setDescription("A detailed description of the test bug.");
        testBug.setPriority(Priority.MEDIUM);
        testBug.setStatus(Status.OPEN);
        testBug.setCreatedAt(LocalDateTime.now().minusDays(1));
        testBug.setUpdatedAt(LocalDateTime.now());

        // Mock the user service call that happens in create/edit forms to populate dropdowns
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(testUser)));
    }

    @Nested
    @DisplayName("GET /bugs - List Bugs")
    class ListBugsTests {
        @Test
        @WithMockUser // Any authenticated user can view
        @DisplayName("Should return the bug list view for an authenticated user")
        void getAllBugs_ShouldReturnBugListPage() throws Exception {
            Page<Bug> bugPage = new PageImpl<>(List.of(testBug));
            when(bugService.getAllBugs(any(Pageable.class))).thenReturn(bugPage);

            mockMvc.perform(get("/bugs"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("bugs/list"))
                    .andExpect(model().attributeExists("bugsPage"))
                    .andExpect(model().attribute("bugsPage", hasProperty("content", hasSize(1))))
                    .andExpect(model().attribute("currentPage", "bugs"));
        }

        @Test
        @DisplayName("Should redirect unauthenticated user to login")
        void getAllBugs_Unauthenticated_ShouldRedirect() throws Exception {
            mockMvc.perform(get("/bugs"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("http://localhost/auth"));
        }
    }

    @Nested
    @DisplayName("GET /bugs/new - Create Bug Form")
    class CreateBugFormTests {
        @Test
        @WithMockUser // Any authenticated user can view the form
        @DisplayName("Should display the form for creating a new bug for an authenticated user")
        void showCreateBugForm_ShouldReturnCreateFormView() throws Exception {
            mockMvc.perform(get("/bugs/new"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("bugs/create"))
                    .andExpect(model().attributeExists("bug"))
                    .andExpect(model().attributeExists("users"))
                    .andExpect(model().attribute("currentPage", "createBug"));

            verify(userService, times(1)).getAllUsers(any(Pageable.class));
        }

        @Test
        @DisplayName("Should redirect unauthenticated user to login")
        void showCreateBugForm_Unauthenticated_ShouldRedirect() throws Exception {
            mockMvc.perform(get("/bugs/new"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("http://localhost/auth"));
        }
    }

    @Nested
    @DisplayName("POST /bugs - Create Bug Submission")
    class CreateBugSubmissionTests {
        @Test
        @WithMockUser // Any authenticated user can create
        @DisplayName("Should create a new bug and redirect to its detail page on success")
        void createBug_WithValidData_ShouldCreateBugAndRedirect() throws Exception {
            when(bugService.createBug(any(Bug.class))).thenReturn(testBug);

            mockMvc.perform(post("/bugs")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("title", testBug.getTitle())
                            .param("description", testBug.getDescription())
                            .param("priority", testBug.getPriority().name())
                            .param("status", testBug.getStatus().name())
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/bugs/" + testBug.getId()))
                    .andExpect(flash().attribute("successMessage", "Bug created successfully!"));

            verify(bugService, times(1)).createBug(any(Bug.class));
        }

        @Test
        @WithMockUser // Any authenticated user can create
        @DisplayName("Should return to create form with errors if data is invalid")
        void createBug_WithInvalidData_ShouldReturnFormWithErrors() throws Exception {
            // This test expects the controller to have @Valid on the @ModelAttribute
            mockMvc.perform(post("/bugs")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("title", "") // Invalid: empty title
                            .param("description", "Some description")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("bugs/create"))
                    .andExpect(model().attributeHasFieldErrors("bug", "title"))
                    .andExpect(model().attributeExists("users")) // Ensure users are re-added
                    .andExpect(model().attribute("currentPage", "createBug"));

            verify(bugService, never()).createBug(any(Bug.class));
            verify(userService, times(1)).getAllUsers(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /bugs/{id} - View Bug Details")
    class ViewBugDetailsTests {
        @Test
        @WithMockUser // Any authenticated user can view
        @DisplayName("Should display bug details for a valid bug ID for an authenticated user")
        void getBugById_WithValidId_ShouldReturnDetailView() throws Exception {
            when(bugService.getBugById(1L)).thenReturn(testBug);

            mockMvc.perform(get("/bugs/1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("bugs/detail"))
                    .andExpect(model().attribute("bug", hasProperty("id", is(1L))))
                    .andExpect(model().attribute("currentPage", "bugDetail"));
        }

        @Test
        @WithMockUser // Any authenticated user can view
        @DisplayName("Should redirect to bug list if bug ID is not found for an authenticated user")
        void getBugById_WithInvalidId_ShouldRedirectToList() throws Exception {
            String errorMessage = "Bug not found with id: 99";
            when(bugService.getBugById(99L)).thenThrow(new NotFoundException(errorMessage));

            mockMvc.perform(get("/bugs/99"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/bugs"))
                    .andExpect(flash().attribute("errorMessage", errorMessage));
        }
    }

    @Nested
    @DisplayName("GET /bugs/{id}/edit - Edit Bug Form")
    class EditBugFormTests {
        @Test
        @WithMockUser // Any authenticated user can view
        @DisplayName("Should display the edit form for a valid bug ID for an authenticated user")
        void showEditBugForm_WithValidId_ShouldReturnEditForm() throws Exception {
            when(bugService.getBugById(1L)).thenReturn(testBug);

            mockMvc.perform(get("/bugs/1/edit"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("bugs/edit"))
                    .andExpect(model().attribute("bug", hasProperty("id", is(1L))))
                    .andExpect(model().attributeExists("users"))
                    .andExpect(model().attribute("currentPage", "editBug"));

            verify(userService, times(1)).getAllUsers(any(Pageable.class));
        }

        @Test
        @WithMockUser // Any authenticated user can view
        @DisplayName("Should redirect to bug list if bug ID for editing is not found for an authenticated user")
        void showEditBugForm_WithInvalidId_ShouldRedirectToList() throws Exception {
            String errorMessage = "Bug not found with id: 99";
            when(bugService.getBugById(99L)).thenThrow(new NotFoundException(errorMessage));

            mockMvc.perform(get("/bugs/99/edit"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/bugs"))
                    .andExpect(flash().attribute("errorMessage", errorMessage));
        }
    }

    @Nested
    @DisplayName("POST /bugs/{id}/edit - Update Bug Submission")
    class UpdateBugSubmissionTests {
        @Test
        @WithMockUser // Any authenticated user can update
        @DisplayName("Should update bug and redirect to detail page on success")
        void updateBug_WithValidData_ShouldUpdateAndRedirect() throws Exception {
            when(bugService.updateBug(eq(1L), any(Bug.class))).thenReturn(testBug);

            mockMvc.perform(post("/bugs/1/edit")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("title", "Updated Title")
                            .param("description", "Updated description.")
                            .param("priority", "HIGH")
                            .param("status", "IN_PROGRESS")
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/bugs/1"))
                    .andExpect(flash().attribute("successMessage", "Bug updated successfully!"));

            verify(bugService, times(1)).updateBug(eq(1L), any(Bug.class));
        }

        @Test
        @WithMockUser // Any authenticated user can update
        @DisplayName("Should return to edit form with errors if data is invalid")
        void updateBug_WithInvalidData_ShouldReturnFormWithErrors() throws Exception {
            // This test expects the controller to have @Valid on the @ModelAttribute.
            // The surefire report shows this test failing with a 302, which means
            // validation is not being triggered. Adding @Valid in the controller fixes this.
            mockMvc.perform(post("/bugs/1/edit")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("title", "") // Invalid: empty title
                            .param("description", "Updated description.")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("bugs/edit"))
                    .andExpect(model().attributeHasFieldErrors("bug", "title"))
                    .andExpect(model().attributeExists("users")) // Ensure users are re-added
                    .andExpect(model().attribute("currentPage", "editBug"));

            verify(bugService, never()).updateBug(anyLong(), any(Bug.class));
            verify(userService, times(1)).getAllUsers(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("POST /bugs/{id}/delete - Delete Bug")
    class DeleteBugTests {
        @Test
        @WithMockUser(roles = "ADMIN") // Only ADMIN can delete
        @DisplayName("Should delete bug and redirect to list on success")
        void deleteBug_WithValidId_ShouldDeleteAndRedirect() throws Exception {
            doNothing().when(bugService).deleteBug(1L);

            mockMvc.perform(post("/bugs/1/delete")
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/bugs"))
                    .andExpect(flash().attribute("successMessage", "Bug deleted successfully!"));

            verify(bugService, times(1)).deleteBug(1L);
        }

        @Test
        @WithMockUser(roles = "ADMIN") // Only ADMIN can delete
        @DisplayName("Should redirect to list with error if bug to delete is not found")
        void deleteBug_WithInvalidId_ShouldRedirectWithError() throws Exception {
            String errorMessage = "Cannot delete: Bug not found with id: 99";
            doThrow(new NotFoundException(errorMessage)).when(bugService).deleteBug(99L);

            mockMvc.perform(post("/bugs/99/delete")
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/bugs"))
                    .andExpect(flash().attribute("errorMessage", errorMessage));

            verify(bugService, times(1)).deleteBug(99L);
        }

        @Test
        @WithMockUser(roles = "USER") // A regular user
        @DisplayName("Should be Forbidden for non-ADMIN user")
        void deleteBug_AsUser_ShouldBeForbidden() throws Exception {
            mockMvc.perform(post("/bugs/1/delete")
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(bugService, never()).deleteBug(anyLong());
        }
    }
}
