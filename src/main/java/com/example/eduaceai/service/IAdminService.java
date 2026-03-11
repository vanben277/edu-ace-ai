package com.example.eduaceai.service;

import com.example.eduaceai.dto.req.UserFilterForm;
import com.example.eduaceai.dto.res.UserResponse;

import java.util.List;

public interface IAdminService {
    List<UserResponse> getAllUsers(UserFilterForm form);

    void toggleUserStatus(Long userId);
}
