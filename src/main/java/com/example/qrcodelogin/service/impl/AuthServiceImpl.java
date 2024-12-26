package com.example.qrcodelogin.service.impl;

import com.example.qrcodelogin.model.LoginRequest;
import com.example.qrcodelogin.model.LoginResponse;
import com.example.qrcodelogin.service.AuthService;
import com.example.qrcodelogin.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtUtil jwtUtil;

    @Override
    public LoginResponse login(LoginRequest request) {
        // 这里简化了用户验证逻辑，实际项目中需要查询数据库验证用户名密码
        if ("admin".equals(request.getUsername()) && "password".equals(request.getPassword())) {
            LoginResponse response = new LoginResponse();
            response.setUsername(request.getUsername());
            response.setToken(jwtUtil.generateToken(request.getUsername()));
            return response;
        }
        throw new RuntimeException("Invalid username or password");
    }
} 