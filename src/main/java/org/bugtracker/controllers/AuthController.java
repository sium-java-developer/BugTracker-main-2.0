package org.bugtracker.controllers;

import jakarta.validation.Valid;
import org.bugtracker.entities.User;
import org.bugtracker.problems.UserAlreadyExistsException;
import org.bugtracker.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("currentPage", "register"); // Add this line
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result,
                               RedirectAttributes redirectAttributes, Model model) { // Add Model here
        if (result.hasErrors()) {
            model.addAttribute("currentPage", "register"); // Add this line
            return "auth/register";
        }
        try {
            userService.registerUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please log in.");
            return "redirect:/auth";
        } catch (UserAlreadyExistsException e) {
            model.addAttribute("registrationError", e.getMessage());
            model.addAttribute("currentPage", "register"); // Add this line
            return "auth/register";
        } catch (Exception e) {
            model.addAttribute("registrationError", "An unexpected error occurred during registration. Please try again.");
            model.addAttribute("currentPage", "register"); // Add this line
            return "auth/register";
        }
    }
}
