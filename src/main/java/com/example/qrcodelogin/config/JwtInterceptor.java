package com.example.qrcodelogin.config;

import com.example.qrcodelogin.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        log.debug("Received authorization header: {}", authHeader);
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.error("Authorization header is missing or invalid format");
            throw new SecurityException("Missing or invalid authorization header");
        }
        
        try {
            String token = authHeader.substring(7);
            log.debug("Extracted token: {}", token);
            
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                log.debug("Token validated successfully for user: {}", username);
                request.setAttribute("username", username);
                return true;
            } else {
                log.error("Token validation failed");
                throw new SecurityException("Token validation failed");
            }
        } catch (Exception e) {
            log.error("Error processing token: {}", e.getMessage());
            throw new SecurityException("Invalid token: " + e.getMessage());
        }
    }
} 