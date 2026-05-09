package com.dentis.DENTIS.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupVerifier implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupVerifier.class);

    @Value("${spring.security.oauth2.client.registration.google.client-id:NOT_SET}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret:NOT_SET}")
    private String clientSecret;

    @Override
    public void run(ApplicationArguments args) {
        log.info("=== OAuth2 Config Check ===");
        log.info("Client ID   : {}", clientId.isBlank() ? "EMPTY (not loaded!)" : "SET — " + clientId.substring(0, 12) + "...");
        log.info("Client Secret: {}", clientSecret.isBlank() ? "EMPTY (not loaded!)" : "SET — starts with: " + clientSecret.substring(0, 8) + "...");
        log.info("===========================");
    }
}
