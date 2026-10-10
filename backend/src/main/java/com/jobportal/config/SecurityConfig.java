package com.jobportal.config;

import com.jobportal.security.JwtAuthenticationFilter;
import com.jobportal.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserService userService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(UserService userService, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userService = userService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/jobs", "/api/jobs/**").permitAll()
                // Job management — post/edit/delete
                .requestMatchers(HttpMethod.POST, "/api/jobs", "/api/jobs/**").hasAnyRole(
                    "RECRUITER", "ADMIN", "HR_MANAGER", "COMPANY_ADMIN",
                    "HIRING_MANAGER", "DEPARTMENT_HEAD", "MODERATOR")
                .requestMatchers(HttpMethod.PUT, "/api/jobs/**").hasAnyRole(
                    "RECRUITER", "ADMIN", "HR_MANAGER", "COMPANY_ADMIN",
                    "HIRING_MANAGER", "DEPARTMENT_HEAD", "MODERATOR")
                .requestMatchers(HttpMethod.DELETE, "/api/jobs/**").hasAnyRole(
                    "RECRUITER", "ADMIN", "HR_MANAGER", "COMPANY_ADMIN",
                    "HIRING_MANAGER", "DEPARTMENT_HEAD")
                // Applications — submit
                .requestMatchers(HttpMethod.POST, "/api/applications").hasAnyRole(
                    "JOB_SEEKER", "FRESHER", "EXPERIENCED")
                // Applications — update status
                .requestMatchers(HttpMethod.PUT, "/api/applications/*/status").hasAnyRole(
                    "RECRUITER", "ADMIN", "HR_MANAGER", "INTERVIEWER", "COMPANY_ADMIN",
                    "HIRING_MANAGER", "TECHNICAL_LEAD", "DEPARTMENT_HEAD")
                // Admin endpoints
                .requestMatchers("/api/admin/**").hasAnyRole(
                    "ADMIN", "COMPANY_ADMIN", "DEPARTMENT_HEAD")
                .requestMatchers("/api/recruiter/**").hasAnyRole(
                    "RECRUITER", "ADMIN", "HR_MANAGER", "COMPANY_ADMIN",
                    "HIRING_MANAGER", "SOURCER", "DEPARTMENT_HEAD")
                .requestMatchers(HttpMethod.GET, "/api/users").hasAnyRole(
                    "ADMIN", "COMPANY_ADMIN", "SUPPORT_AGENT", "DEPARTMENT_HEAD")
                .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasAnyRole(
                    "ADMIN", "COMPANY_ADMIN")
                // Static + SPA
                .requestMatchers("/", "/index.html", "/assets/**", "/favicon.svg",
                        "/sign-in", "/register", "/dashboard/**").permitAll()
                .requestMatchers("/api/users/**").authenticated()
                .requestMatchers("/api/applications/**").authenticated()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationManager(authenticationManager)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(List.of(provider));
    }
}
