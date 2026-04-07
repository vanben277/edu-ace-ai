package com.example.eduaceai.service;

import com.example.eduaceai.dto.req.LoginRequest;
import com.example.eduaceai.dto.req.RegisterRequest;
import com.example.eduaceai.dto.res.AuthResponse;
import com.example.eduaceai.entity.User;
import jakarta.validation.Valid;

public interface IAuthService {
    User register(RegisterRequest req);

    AuthResponse login(@Valid LoginRequest req);
}
