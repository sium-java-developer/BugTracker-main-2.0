package org.bugtracker.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.ComponentScan; // Often redundant
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver; // Correct import
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
// import org.springframework.web.servlet.mvc.WebContentInterceptor; // Consider if really needed

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;

@Configuration
// @ComponentScan(basePackages = {"org.bugtracker"}) // Usually redundant if this class is in the main app package or sub-package
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public MessageSource messageSource() { // Made public
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        // Set to false, to rely strictly on your defined bundles first.
        // If a key is not found, it will then use the code as the default message (due to setUseCodeAsDefaultMessage).
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setUseCodeAsDefaultMessage(true);
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        // You can specify multiple basenames if you have different message files
        // e.g., "classpath:i18n/messages", "classpath:i18n/validation"
        messageSource.setBasenames("classpath:i18n/messages", "classpath:i18n/global"); // Example with two basenames
        // Cache messages for an hour in production; for development, you might want a shorter duration or -1
        messageSource.setCacheSeconds((int) Duration.ofHours(1).toSeconds());
        return messageSource;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() { // Made public
        LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("lang"); // e.g., /some-url?lang=fr
        return localeChangeInterceptor;
    }

    @Bean
    public LocaleResolver localeResolver() { // Made public, standard bean name
        CookieLocaleResolver cookieLocaleResolver = new CookieLocaleResolver("user_locale"); // Changed cookie name for clarity
        cookieLocaleResolver.setDefaultLocale(Locale.ENGLISH);
        cookieLocaleResolver.setCookieMaxAge(Duration.ofDays(30)); // Store locale preference for 30 days
        cookieLocaleResolver.setCookieHttpOnly(true); // Enhance security
        cookieLocaleResolver.setCookiePath("/"); // Make cookie available for all paths
        return cookieLocaleResolver;
    }

    /*
    // WebContentInterceptor for cache control:
    // It's generally better to manage static resource caching via application.properties:
    // spring.web.resources.cache.period=3600 (for 1 hour)
    // spring.web.resources.cache.cachecontrol.max-age=3600
    // spring.web.resources.cache.cachecontrol.no-store=false (allow caching)
    //
    // If you need to explicitly prevent caching for specific dynamic API endpoints,
    // you can use WebContentInterceptor or CacheControl headers directly in your controllers.
    // Applying it globally with setCacheSeconds(0) is usually too aggressive.

    @Bean
    public WebContentInterceptor webContentInterceptor() { // Made public
        WebContentInterceptor webContentInterceptor = new WebContentInterceptor();
        // Example: Disable caching only for API paths
        // webContentInterceptor.addCacheMapping(CacheControl.noCache().mustRevalidate(), "/api/**");
        webContentInterceptor.setCacheSeconds(0); // Disables HTTP caching headers for matched paths
        webContentInterceptor.setSupportedMethods("GET", "POST", "PUT", "DELETE");
        return webContentInterceptor;
    }
    */

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor())
                .addPathPatterns("/**") // Apply to all application paths
                .excludePathPatterns("/static/**",
                        "/js/**", "/images/**", "/webjars/**", "/favicon.ico"); // Exclude static resources

        // If you decide to use WebContentInterceptor for specific paths:
        // registry.addInterceptor(webContentInterceptor()).addPathPatterns("/api/**");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Redirects the root path "/" to "/home"
        registry.addRedirectViewController("/", "/home");

        // Maps the URL "/auth" directly to the view named "auth" (e.g., auth.html)
        // without needing a controller method.
        registry.addViewController("/auth").setViewName("auth");

        // You can add more view controllers here if needed, for example:
        // registry.addViewController("/register").setViewName("registration");
    }
}