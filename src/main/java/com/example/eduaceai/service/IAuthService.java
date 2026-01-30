package com.example.eduaceai.service;

import com.example.eduaceai.dto.req.LoginRequest;
import com.example.eduaceai.dto.req.RegisterRequest;
import com.example.eduaceai.dto.res.AuthResponse;
import com.example.eduaceai.entity.User;

public interface IAuthService {
    User register(RegisterRequest req);

    AuthResponse login(LoginRequest req);
}
