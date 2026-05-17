package com.dentis.DENTIS.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Serves runtime-uploaded files (prosthodontics documents/photos) from the
 * filesystem so they are accessible even in a running dev server where the JVM
 * classpath was already scanned at startup.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final String UPLOAD_FS_PATH =
            System.getProperty("user.dir") + "/src/main/resources/static/uploads/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + UPLOAD_FS_PATH);
    }
}
