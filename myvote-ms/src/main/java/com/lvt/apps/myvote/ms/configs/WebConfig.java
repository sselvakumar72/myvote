package com.lvt.apps.myvote.ms.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final RequestValidationInterceptor requestValidationInterceptor;

    public WebConfig(RequestValidationInterceptor requestValidationInterceptor) {
        this.requestValidationInterceptor = requestValidationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestValidationInterceptor);
    }
}
