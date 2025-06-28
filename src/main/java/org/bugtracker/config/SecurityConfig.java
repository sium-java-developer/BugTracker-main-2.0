package org.bugtracker.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configures the application's security settings using modern Spring Security 6+ patterns.
 * This class centralizes authentication, authorization, and web-layer security rules.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Defines the main security filter chain that protects application endpoints.
     * This is the central place to configure URL-based security.
     *
     * @param http The HttpSecurity object to be configured.
     * @return The configured SecurityFilterChain.
     * @throws Exception If an error occurs during configuration.
     */
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests(authorize -> authorize
//                        // 1. Grant public access to static resources and essential public pages.
//                        .requestMatchers(
//                                "/", "/home", "/about",          // Public pages
//                                "/auth", "/register",             // Authentication and registration pages
//                                "/css/**", "/js/**", "/images/**", // Static resources
//                                "/favicon.ico",
//                                "/h2-console/**"                  // H2 Database Console (for development)
//                        ).permitAll()
//
//                        // 2. Restrict user management pages to users with the 'ADMIN' role.
//                        .requestMatchers("/users", "/users/**").hasRole("ADMIN")
//
//                        // 3. Secure all other application URLs, requiring users to be authenticated.
//                        // This includes bug management, profile pages, etc.
//                        .anyRequest().authenticated()
//                )
//                .formLogin(form -> form
//                        .loginPage("/auth")           // Specifies the custom login page URL.
//                        .loginProcessingUrl("/login") // The URL where the login form is submitted.
//                        .defaultSuccessUrl("/", true) // Redirect to the home page on successful login.
//                        .failureUrl("/auth?error")    // Redirect back to the login page with an error parameter.
//                        .permitAll()                  // The login page itself must be accessible to everyone.
//                )
//                .logout(logout -> logout
//                        .logoutUrl("/logout")                   // The URL to trigger a logout.
//                        .logoutSuccessUrl("/auth?logout")       // Redirect to the login page with a logout parameter.
//                        .invalidateHttpSession(true)            // Invalidate the user's session.
//                        .deleteCookies("JSESSIONID")            // Delete the session cookie.
//                        .permitAll()
//                )
//                // CSRF is enabled by default. We only need to configure it to ignore the H2 console.
//                .csrf(csrf -> csrf
//                        .ignoringRequestMatchers("/h2-console/**")
//                )
//                // The H2 console runs in a frame, so we need to configure headers to allow this.
//                .headers(headers -> headers
//                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
//                );
//
//        return http.build();
//    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // 1. CRITICAL FIX: Permit access to common static resources (CSS, JS, images, favicon)
                        // This rule MUST come before any authenticated() or more specific rules
                        // to ensure static files are accessible without authentication.
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()

                        // 2. Permit access to public pages (home, login, register, about)
                        .requestMatchers("/", "/home", "/about", "/auth", "/register").permitAll()

                        // 3. Allow H2 console access (for development)
                        .requestMatchers("/h2-console/**").permitAll()

                        // 4. Restrict user management pages to users with the 'ADMIN' role.
                        .requestMatchers("/users", "/users/**").hasRole("ADMIN")

                        // 5. All other requests must be authenticated.
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/auth")           // Specifies the custom login page URL.
                        .loginProcessingUrl("/login") // The URL where the login form is submitted.
                        .defaultSuccessUrl("/", true) // Redirect to the home page on successful login.
                        .failureUrl("/auth?error")    // Redirect back to the login page with an error parameter.
                        .permitAll()                  // The login page itself must be accessible to everyone.
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")                   // The URL to trigger a logout.
                        .logoutSuccessUrl("/auth?logout")       // Redirect to the login page with a logout parameter.
                        .invalidateHttpSession(true)            // Invalidate the user's session.
                        .deleteCookies("JSESSIONID")            // Delete the session cookie.
                        .permitAll()
                )
                // CSRF is enabled by default. We only need to configure it to ignore the H2 console.
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**")
                )
                // The H2 console runs in a frame, so we need to configure headers to allow this.
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                );

        return http.build();
    }

}