package org.bugtracker.controllers;

import jakarta.validation.Valid;
import org.bugtracker.entities.User;
import org.bugtracker.problems.NotFoundException;
import org.bugtracker.problems.UserAlreadyExistsException;
import org.bugtracker.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * Controller for handling user-related web requests, including
 * personal profile management and administrative user management.
 */
@Controller
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // --- Current User Profile Methods ---

    /**
     * Displays the profile page for the currently logged-in user.
     */
    @GetMapping("/profile")
    public String getCurrentUserProfile(Model model, Principal principal) {
        User currentUser = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + principal.getName()));

        model.addAttribute("user", currentUser);
        model.addAttribute("currentPage", "profile");
        return "users/profile";
    }

    /**
     * Displays the form to edit the current user's profile.
     */
    // @GetMapping("/profile/edit")
    // public String showEditProfileForm(Model model, Principal principal) {
    //     User currentUser = userService.findByUsername(principal.getName())
    //             .orElseThrow(() -> new UsernameNotFoundException("User not found: " + principal.getName()));

    //     // Create and populate a DTO to safely expose user data to the form
    //     UserProfileUpdateDto userDto = new UserProfileUpdateDto();
    //     userDto.setUsername(currentUser.getUsername());
    //     userDto.setFirstName(currentUser.getFirstName());
    //     userDto.setLastName(currentUser.getLastName());
    //     userDto.setEmail(currentUser.getEmail());

    //     model.addAttribute("userDto", userDto);
    //     return "users/profile-edit";
    // }

    /**
     * Processes the submission of the profile update form.
     */
    // @PostMapping("/profile/update")
    // public String updateCurrentUserProfile(@Valid @ModelAttribute("userDto") UserProfileUpdateDto userDto,
    //                                        BindingResult bindingResult,
    //                                        Principal principal,
    //                                        RedirectAttributes redirectAttributes) {

    //     // If validation fails, return to the form with the submitted data and error messages.
    //     if (bindingResult.hasErrors()) {
    //         logger.warn("Validation errors for user '{}' during profile update.", principal.getName());
    //         // **THE FIX**: The username is read-only and not part of the form submission,
    //         // so we must re-add it to the DTO before returning to the view.
    //         userDto.setUsername(principal.getName());
    //         return "users/profile-edit";
    //     }

    //     try {
    //         userService.updateUserProfile(principal.getName(), userDto);
    //         redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully.");
    //         logger.info("Profile updated for user: {}", principal.getName());
    //         return "redirect:/users/profile";
    //     } catch (UserAlreadyExistsException e) {
    //         logger.warn("Profile update failed for user '{}' due to conflict: {}", principal.getName(), e.getMessage());
    //         bindingResult.rejectValue("email", "email.exists", e.getMessage());
    //         userDto.setUsername(principal.getName()); // Also re-add username here
    //         return "users/profile-edit";
    //     } catch (Exception e) {
    //         logger.error("Error updating profile for user {}: {}", principal.getName(), e.getMessage(), e);
    //         redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred. Please try again.");
    //         return "redirect:/users/profile/edit";
    //     }
    // }


    // --- Admin-only User Management Methods ---

    /**
     * Displays a paginated list of all users. Accessible only by admins.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String listUsers(Model model, @PageableDefault(size = 10, sort = "username") Pageable pageable) {
        Page<User> usersPage = userService.getAllUsers(pageable);
        model.addAttribute("usersPage", usersPage);
        model.addAttribute("currentPage", "users");
        return "users/list";
    }

    /**
     * Displays the form for an admin to create a new user.
     */
    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateUserForm(Model model) {
        model.addAttribute("user", new User());
        return "users/create";
    }

    /**
     * Processes the creation of a new user by an admin.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String createUser(@Valid @ModelAttribute("user") User user,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "users/create";
        }
        try {
            userService.createUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "User '" + user.getUsername() + "' created successfully.");
            logger.info("Admin created new user: {}", user.getUsername());
            return "redirect:/users";
        } catch (UserAlreadyExistsException e) {
            logger.warn("Admin user creation failed: {}", e.getMessage());
            bindingResult.rejectValue("username", "username.exists", e.getMessage());
            return "users/create";
        }
    }

    /**
     * Displays the form for an admin to edit an existing user.
     */
    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditUserForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserById(id)
                    .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
            model.addAttribute("user", user);
            return "users/edit";
        } catch (NotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/users";
        }
    }

    /**
     * Processes the update of an existing user by an admin.
     */
    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateUser(@PathVariable Long id,
                             @Valid @ModelAttribute("user") User user,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "users/edit";
        }
        try {
            user.setId(id); // Ensure the ID from the path is set on the user object
            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "User '" + user.getUsername() + "' updated successfully.");
            logger.info("Admin updated user with ID: {}", id);
            return "redirect:/users";
        } catch (NotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/users";
        } catch (UserAlreadyExistsException e) {
            bindingResult.rejectValue("email", "email.exists", e.getMessage());
            return "users/edit";
        }
    }

    /**
     * Handles the deletion of a user by an admin.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully.");
            logger.info("Admin deleted user with ID: {}", id);
        } catch (NotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            logger.error("Error deleting user with id {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user. It might be associated with other data.");
        }
        return "redirect:/users";
    }
}