package com.example.clients_management.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadDir = "file:uploads/";
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadDir)
                .setCachePeriod(3600)
                .resourceChain(true);
                
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600)
                .resourceChain(true);
    }
}
