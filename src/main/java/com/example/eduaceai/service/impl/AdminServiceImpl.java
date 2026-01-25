package com.example.eduaceai.service.impl;

import com.example.eduaceai.dto.req.UserFilterForm;
import com.example.eduaceai.entity.User;
import com.example.eduaceai.repository.UserRepository;
import com.example.eduaceai.repository.specification.UserSpecification;
import com.example.eduaceai.service.IAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements IAdminService {

    private final UserRepository userRepository;

    @Override
    public List<User> getAllUsers(UserFilterForm form) {
        String sortBy = (form.sortBy() != null && !form.sortBy().isEmpty()) ? form.sortBy() : "createdAt";
        Sort sort = (form.sortDir() != null && form.sortDir().equalsIgnoreCase("asc"))
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        var spec = UserSpecification.filterUsers(form);

        return userRepository.findAll(spec, sort);
    }
}