package com.example.qrcodelogin.service;

import com.example.qrcodelogin.model.LoginRequest;
import com.example.qrcodelogin.model.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
} 