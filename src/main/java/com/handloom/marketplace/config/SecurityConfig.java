package com.handloom.marketplace.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig implements WebMvcConfigurer {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            for (GrantedAuthority auth : authentication.getAuthorities()) {
                String role = auth.getAuthority();
                if ("ROLE_ADMIN".equals(role)) {
                    response.sendRedirect("/admin/dashboard");
                    return;
                } else if ("ROLE_ARTISAN".equals(role)) {
                    response.sendRedirect("/artisan/dashboard");
                    return;
                } else if ("ROLE_CUSTOMER".equals(role)) {
                    response.sendRedirect("/");
                    return;
                }
            }
            response.sendRedirect("/");
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Static resources & Uploads
                .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**", "/webjars/**").permitAll()
                // Public browsing pages
                .requestMatchers("/", "/home", "/products", "/products/**", "/categories", "/categories/**", "/artisans", "/artisans/**", "/about", "/contact").permitAll()
                // Auth endpoints
                .requestMatchers("/login", "/register", "/error").permitAll()
                // Role-specific areas
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/artisan/**").hasRole("ARTISAN")
                .requestMatchers("/cart/**", "/checkout/**", "/orders/**", "/reviews/**").hasRole("CUSTOMER")
                // Any other request requires authentication
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(customAuthenticationSuccessHandler())
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded artisan product images from disk
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:e:/handloom-marketplace/uploads/");
    }
}
