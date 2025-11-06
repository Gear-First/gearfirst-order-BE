package com.gearfirst.backend.common.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    @Bean
    public FilterRegistrationBean<JwtHeaderFilter> jwtHeaderFilter() {
        FilterRegistrationBean<JwtHeaderFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new JwtHeaderFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }
}
