package com.gearfirst.backend.common.config.webconfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 URL에 대해
                .allowedOrigins(
                        "http://localhost:5173",
                        "https://gearfirst-fe.vercel.app"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")   // 모든 헤더 허용
                .allowCredentials(true) // 쿠키 인증 허용
                .maxAge(3600);        // 프리플라이트 응답 캐시 시간(초)
    }
}
