package com.gearfirst.backend.common.filter;

import com.gearfirst.backend.common.context.UserContext;
import com.gearfirst.backend.common.context.UserContextHolder;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class JwtHeaderFilter implements Filter {

    //실제 요청 처리흐름을 가로 채는 메서드
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        //ServletRequest를 HTTP 요청 객체로 다운캐스팅
        //헤더를 읽기 위해 HttpServletRequest가 필요함
        HttpServletRequest httpReq = (HttpServletRequest) servletRequest;

        String userId = httpReq.getHeader("X-User-Id");
        String name = decode(httpReq.getHeader("X-User-Name"));
        String rank = decode(httpReq.getHeader("X-User-Rank"));
        String region = decode(httpReq.getHeader("X-User-Region"));
        String workType = decode(httpReq.getHeader("X-User-WorkType"));

        if (userId != null) {
            UserContextHolder.set(new UserContext(userId, name, rank, region, workType));
        }

        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContextHolder.clear();
        }
    }
    private String decode(String value) {
        return value != null
                ? new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8)
                : null;
    }

}
