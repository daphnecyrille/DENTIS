package com.dentis.DENTIS.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Serves runtime-uploaded files (prosthodontics documents/photos) from the
 * filesystem so they are accessible even in a running dev server where the JVM
 * classpath was already scanned at startup.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final ActivityLogInterceptor activityLogInterceptor;
    private final ChartOwnerInterceptor chartOwnerInterceptor;

    public WebMvcConfig(ActivityLogInterceptor activityLogInterceptor, ChartOwnerInterceptor chartOwnerInterceptor) {
        this.activityLogInterceptor = activityLogInterceptor;
        this.chartOwnerInterceptor = chartOwnerInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(chartOwnerInterceptor);    // charts from a chart request are read-only
        registry.addInterceptor(activityLogInterceptor);   // writes the activity log after important actions
    }

    // DENTIS_UPLOAD_DIR is set on the hosted server (Vercel), where only /tmp is writable.
    public static final String UPLOAD_FS_PATH = System.getenv().getOrDefault(
            "DENTIS_UPLOAD_DIR", System.getProperty("user.dir") + "/src/main/resources/static/uploads/");

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + UPLOAD_FS_PATH);
    }
}
