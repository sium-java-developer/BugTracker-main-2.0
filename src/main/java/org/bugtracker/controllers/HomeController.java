package org.bugtracker.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("currentPage", "home"); // Add this line
        return "home";
    }

    @GetMapping("/auth")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        if (error != null) {
            model.addAttribute("loginError", "Invalid username or password.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "You have been logged out successfully.");
        }
        model.addAttribute("currentPage", "auth"); // Add this line
        return "auth/login";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("currentPage", "about"); // Add this line
        return "about";
    }
}