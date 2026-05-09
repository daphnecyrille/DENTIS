package com.dentis.DENTIS.service;

import com.dentis.DENTIS.model.AccountStatus;
import com.dentis.DENTIS.model.User;
import com.dentis.DENTIS.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String email = oAuth2User.getAttribute("email");
        log.info("OAuth2 login attempt for email: {}", email);

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            log.warn("Login denied — no account registered for email: {}", email);
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("access_denied"),
                    "No account registered for: " + email);
        }

        log.info("Found user: {} | role: {} | status: {}", email, user.getRole(), user.getStatus());

        if (user.getStatus() == AccountStatus.INACTIVE) {
            log.warn("Login denied — account inactive: {}", email);
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("account_inactive"), "Account is inactive.");
        }
        if (user.getStatus() == AccountStatus.LOCKED) {
            log.warn("Login denied — account locked: {}", email);
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("account_locked"), "Account is locked.");
        }

        log.info("Login successful for: {}", email);
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                oAuth2User.getAttributes(),
                userNameAttributeName);
    }
}
