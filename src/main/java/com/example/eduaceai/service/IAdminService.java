package com.example.eduaceai.service;

import com.example.eduaceai.dto.req.UserFilterForm;
import com.example.eduaceai.entity.User;

import java.util.List;

public interface IAdminService {
    List<User> getAllUsers(UserFilterForm form);

    void toggleUserStatus(Long userId);
}
