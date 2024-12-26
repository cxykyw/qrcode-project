package com.example.qrcodelogin.model;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String username;
} 