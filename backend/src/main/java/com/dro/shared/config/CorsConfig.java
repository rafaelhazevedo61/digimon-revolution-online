package com.dro.shared.config;

import com.dro.shared.security.AdminAuthInterceptor;
import com.dro.shared.security.TokenVersionInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final AdminAuthInterceptor adminAuthInterceptor;
    private final TokenVersionInterceptor tokenVersionInterceptor;

    @Value("${dro.cors.allowed-origins}")
    private String allowedOrigins;

    public CorsConfig (
            AdminAuthInterceptor adminAuthInterceptor,
            TokenVersionInterceptor tokenVersionInterceptor
    ) {
        this.adminAuthInterceptor = adminAuthInterceptor;
        this.tokenVersionInterceptor = tokenVersionInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        String[] origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toArray(String[]::new);

        registry.addMapping("/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenVersionInterceptor)
                .addPathPatterns("/**");
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/admin/**");
    }
}