package com.dentis.DENTIS.config;

import com.dentis.DENTIS.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

@Configuration
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService,
                          OAuth2SuccessHandler oAuth2SuccessHandler,
                          OAuth2FailureHandler oAuth2FailureHandler) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.oAuth2FailureHandler = oAuth2FailureHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/images/**", "/js/**", "/login", "/oauth2/**").permitAll()
                .requestMatchers(
                    "/faculty-dashboard", "/faculty-assign", "/faculty-patientlist",
                    "/chartsview-faculty/**", "/faculty-dashboard/**", "/admitting-view-faculty/**",
                    "/oralsurgery-view-faculty/**", "/oralsurgery2-view-faculty/**",
                    "/endodontics-view-faculty/**", "/endodontics2-view-faculty/**"
                ).hasRole("FACULTY")
                .requestMatchers(
                    "/dashboard-clinicmanager", "/patientlist-clinicmanager", "/requestlist-clinicmanager",
                    "/chartsview-clinicmanager/**", "/admitting-clinicmanager/**",
                    "/admitting-view-clinicmanager/**"
                ).hasRole("CLINIC_MANAGER")
                .requestMatchers(
                    "/clinician-dashboard", "/chartsview-clinician/**", "/chartrequest-clinician",
                    "/patientlist-clinician", "/admitting-edit-clinician/**",
                    "/admitting-view-clinician/**", "/oralsurgery-clinician/**", "/oralsurgery2-clinician/**",
                    "/endodontics-clinician/**", "/endodontics2-clinician/**", "/periodonticsa-clinician/**", "/periodonticsb-clinician/**"
                ).hasRole("CLINICIAN")
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService))
                .successHandler(oAuth2SuccessHandler)
                .failureHandler(oAuth2FailureHandler)
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
            )
            .exceptionHandling(ex -> ex
                .accessDeniedHandler(roleBasedAccessDeniedHandler())
            )
            .csrf(csrf -> csrf.disable());

        return http.build();
    }

    private AccessDeniedHandler roleBasedAccessDeniedHandler() {
        return (HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex) -> {
            Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            String redirect = "/login";
            if (auth != null) {
                if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_FACULTY"))) {
                    redirect = "/faculty-dashboard";
                } else if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CLINIC_MANAGER"))) {
                    redirect = "/dashboard-clinicmanager";
                } else if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CLINICIAN"))) {
                    redirect = "/clinician-dashboard";
                }
            }
            response.sendRedirect(redirect);
        };
    }
}
