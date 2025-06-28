package org.bugtracker.services;

import org.bugtracker.entities.User;
import org.bugtracker.repos.UserRepo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;

/**
 * This service acts as the bridge between Spring Security and the application's user data.
 * It's responsible for loading a user's details by their username. Spring Security will
 * automatically detect and use this bean for authentication.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepo userRepository;

    public CustomUserDetailsService(UserRepo userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Locates the user based on the username. This is the core method used by Spring Security.
     *
     * @param username the username identifying the user whose data is required.
     * @return a fully populated UserDetails object (never null).
     * @throws UsernameNotFoundException if the user could not be found.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find the user from the database using the repository
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with username: " + username));

        // Convert our User entity into a Spring Security UserDetails object
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                getAuthorities(user)
        );
    }

    /**
     * Helper method to convert the user's admin status into a collection of GrantedAuthority.
     * Spring Security uses this to handle authorization (e.g., @PreAuthorize("hasRole('ADMIN')")).
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        if (user.isAdmin()) {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }
}