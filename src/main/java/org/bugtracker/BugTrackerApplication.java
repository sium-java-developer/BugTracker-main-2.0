package org.bugtracker;

import org.bugtracker.entities.Bug;
import org.bugtracker.entities.Priority;
import org.bugtracker.entities.Status;
import org.bugtracker.entities.User;
import org.bugtracker.services.BugService;
import org.bugtracker.services.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder; // Keep this import

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional; 
 
@SpringBootApplication
public class BugTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BugTrackerApplication.class, args);
    }

    @Bean
    @Profile("dev") // This CommandLineRunner will only run when the "dev" profile is active
    public CommandLineRunner dataLoader(UserService userService, BugService bugService, PasswordEncoder passwordEncoder) {
        // PasswordEncoder is now injected directly into the method, which is the recommended pattern for @Bean methods.
        return args -> {
            System.out.println("--- [DIAGNOSTIC DATALOADER RUNNING] ---");

            User jdoe;
            User asmith;

            // Create admin user
            Optional<User> adminUserOptional = userService.findByUsername("jdoe");
            if (adminUserOptional.isEmpty()) {
                System.out.println("Admin user 'jdoe' not found. Creating new admin user.");
                User newAdmin = new User();
                newAdmin.setUsername("jdoe");
                newAdmin.setFirstName("John");
                newAdmin.setLastName("Doe");
                newAdmin.setEmail("jdoe@example.com");
                newAdmin.setAdmin(true);
                // Encode the password before saving!
                newAdmin.setPassword(passwordEncoder.encode("password")); // Use the injected passwordEncoder
                jdoe = userService.save(newAdmin);
                System.out.println("Admin user 'jdoe' created successfully.");
            } else {
                jdoe = adminUserOptional.get();
                System.out.println("Admin user 'jdoe' already exists.");
            }
 
            // Create standard user
            Optional<User> standardUserOptional = userService.findByUsername("asmith");
            if (standardUserOptional.isEmpty()) {
                System.out.println("Standard user 'asmith' not found. Creating new user.");
                User newStandard = new User();
                newStandard.setUsername("asmith");
                newStandard.setFirstName("Alice");
                newStandard.setLastName("Smith");
                newStandard.setEmail("asmith@example.com");
                newStandard.setAdmin(false);
                // Encode the password before saving!
                newStandard.setPassword(passwordEncoder.encode("password")); // Use the injected passwordEncoder
                asmith = userService.save(newStandard);
                System.out.println("Standard user 'asmith' created successfully.");
            } else {
                asmith = standardUserOptional.get();
                System.out.println("Standard user 'asmith' already exists.");
            }

            // Check if there are any bugs, if not, create sample bugs
            if (bugService.count() == 0) {
                System.out.println("No bugs found. Creating sample bugs.");

                Bug bug1 = new Bug();
                bug1.setTitle("Login page unresponsive on mobile");
                bug1.setDescription("When accessing the login page on a mobile device, the input fields are not clickable.");
                bug1.setPriority(Priority.HIGH);
                bug1.setStatus(Status.OPEN);
                bug1.setAssignedUsers(new HashSet<>(Arrays.asList(jdoe)));
                bug1.setCreatedAt(LocalDateTime.now());
                bug1.setUpdatedAt(LocalDateTime.now());
                bugService.save(bug1);

                Bug bug2 = new Bug();
                bug2.setTitle("Dashboard data not refreshing");
                bug2.setDescription("The bug count on the dashboard does not update automatically after a bug is resolved.");
                bug2.setPriority(Priority.MEDIUM);
                bug2.setStatus(Status.IN_PROGRESS);
                bug2.setAssignedUsers(new HashSet<>(Arrays.asList(asmith)));
                bug2.setCreatedAt(LocalDateTime.now());
                bug2.setUpdatedAt(LocalDateTime.now());
                bugService.save(bug2); 

                Bug bug3 = new Bug();
                bug3.setTitle("Incorrect date format in bug details");
                bug3.setDescription("Dates in the bug detail view are displayed in YYYY-MM-DD format instead of DD-MM-YYYY.");
                bug3.setPriority(Priority.LOW);
                bug3.setStatus(Status.OPEN);
                bug3.setAssignedUsers(new HashSet<>(Arrays.asList(jdoe, asmith)));
                bug3.setCreatedAt(LocalDateTime.now());
                bug3.setUpdatedAt(LocalDateTime.now());
                bugService.save(bug3);

                System.out.println("Sample bugs created successfully.");
            } else {
                System.out.println("Sample bugs already exist.");
            }

            System.out.println("--- [DIAGNOSTIC DATALOADER FINISHED] ---");
        };
    }
}