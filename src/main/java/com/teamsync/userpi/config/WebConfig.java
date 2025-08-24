package com.teamsync.userpi.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // This makes sure Angular routes work (SPA fallback)
        registry.addViewController("/{path:^(?!api$).*$}")
                .setViewName("forward:/index.html");
        registry.addViewController("/**/{path:^(?!api$).*$}")
                .setViewName("forward:/index.html");
    }
}
