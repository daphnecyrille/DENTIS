package com.dentis.DENTIS.config;

import com.dentis.DENTIS.model.Role;
import com.dentis.DENTIS.model.User;
import com.dentis.DENTIS.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    public OAuth2SuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        User user = userRepository.findByEmail(email).orElseThrow();

        String redirectUrl = resolveRedirectUrl(user.getRole());
        response.sendRedirect(redirectUrl);
    }

    private String resolveRedirectUrl(Role role) {
        return switch (role) {
            case CLINICIAN -> "/clinician-dashboard";
            case FACULTY -> "/clinician-dashboard";
            case CLINIC_MANAGER -> "/dashboard-clinicmanager";
            case ADMIN -> "/clinician-dashboard";
        };
    }
}
