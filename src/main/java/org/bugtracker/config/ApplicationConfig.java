package org.bugtracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * General application configuration, including common beans.
 */
@Configuration
public class ApplicationConfig {

    /**
     * Defines the password encoder bean. BCrypt is the industry standard.
     * This bean will be used for both encoding passwords when saving users
     * and for verifying passwords during login.
     *
     * @return A BCryptPasswordEncoder instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}