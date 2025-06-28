package org.bugtracker.controllers;

import jakarta.validation.Valid;
import org.bugtracker.entities.Bug;
import org.bugtracker.problems.NotFoundException;
import org.bugtracker.services.BugService;
import org.bugtracker.services.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize; // Import for security annotations
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/bugs") // All mappings in this controller start with /bugs
public class BugController {

    private final BugService bugService;
    private final UserService userService;

    public BugController(BugService bugService, UserService userService) {
        this.bugService = bugService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')") // Any authenticated user can view bugs
    public String getAllBugs(Model model, @PageableDefault(size = 10) Pageable pageable) {
        Page<Bug> bugsPage = bugService.getAllBugs(pageable);
        model.addAttribute("bugsPage", bugsPage);
        model.addAttribute("currentPage", "bugs");
        return "bugs/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')") // Any authenticated user can access the create form
    public String showCreateBugForm(Model model) {
        model.addAttribute("bug", new Bug());
        // Corrected: Use userService.getAllUsers to get all users for the dropdown
        model.addAttribute("users", userService.getAllUsers(Pageable.unpaged()).getContent());
        model.addAttribute("currentPage", "createBug");
        return "bugs/create";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')") // Any authenticated user can create a bug
    public String createBug(@Valid @ModelAttribute("bug") Bug bug, BindingResult result,
                            RedirectAttributes redirectAttributes, Model model) {
        if (result.hasErrors()) {
            // If there are validation errors, return to the form view
            // The 'bug' object with errors will be automatically added to the model
            // for Thymeleaf to display.
            // Re-add users to the model for the "Assigned Users" dropdown when returning to the form
            model.addAttribute("users", userService.getAllUsers(Pageable.unpaged()).getContent());
            model.addAttribute("currentPage", "createBug");
            return "bugs/create";
        }
        // The bugService.createBug method should handle setting createdAt, updatedAt, and default Status
        Bug createdBug = bugService.createBug(bug);
        redirectAttributes.addFlashAttribute("successMessage", "Bug created successfully!");
        return "redirect:/bugs/" + createdBug.getId();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')") // Any authenticated user can view a specific bug
    public String getBugById(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Bug bug = bugService.getBugById(id);
            model.addAttribute("bug", bug);
            model.addAttribute("currentPage", "bugDetail");
            return "bugs/detail";
        } catch (NotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/bugs";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')") // Any authenticated user can access the edit form
    public String showEditBugForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Bug bug = bugService.getBugById(id);
            model.addAttribute("bug", bug);
            // Re-add users for dropdown
            model.addAttribute("users", userService.getAllUsers(Pageable.unpaged()).getContent());
            model.addAttribute("currentPage", "editBug");
            return "bugs/edit";
        } catch (NotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/bugs";
        }
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')") // Any authenticated user can update a bug
    public String updateBug(@PathVariable Long id, @Valid @ModelAttribute("bug") Bug bug, BindingResult result,
                            RedirectAttributes redirectAttributes, Model model) {
        if (result.hasErrors()) {
            // Re-add users for dropdown
            model.addAttribute("users", userService.getAllUsers(Pageable.unpaged()).getContent());
            model.addAttribute("currentPage", "editBug");
            return "bugs/edit";
        } 
        try {
            bugService.updateBug(id, bug);
            redirectAttributes.addFlashAttribute("successMessage", "Bug updated successfully!");
            return "redirect:/bugs/" + id;
        } catch (NotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/bugs";
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')") // Only ADMIN can delete bugs
    public String deleteBug(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bugService.deleteBug(id);
            redirectAttributes.addFlashAttribute("successMessage", "Bug deleted successfully!");
        } catch (NotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/bugs";
    }
}
