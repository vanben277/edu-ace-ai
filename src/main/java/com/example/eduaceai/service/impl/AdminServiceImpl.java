package com.example.eduaceai.service.impl;

import com.example.eduaceai.dto.req.UserFilterForm;
import com.example.eduaceai.dto.res.UserResponse;
import com.example.eduaceai.entity.User;
import com.example.eduaceai.exception.BusinessException;
import com.example.eduaceai.repository.UserRepository;
import com.example.eduaceai.repository.specification.UserSpecification;
import com.example.eduaceai.service.IAdminService;
import com.example.eduaceai.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements IAdminService {

    private final UserRepository userRepository;

    @Override
    public List<UserResponse> getAllUsers(UserFilterForm form) {
        String sortBy = (form.sortBy() != null && !form.sortBy().isEmpty()) ? form.sortBy() : "createdAt";
        Sort sort = (form.sortDir() != null && form.sortDir().equalsIgnoreCase("asc"))
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        var spec = UserSpecification.filterUsers(form);

        List<User> users = userRepository.findAll(spec, sort);

        return users.stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getStudentCode(),
                        user.getFullName(),
                        user.getRole().name(),
                        user.isEnabled(),
                        user.getCreatedAt()
                ))
                .toList();
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy người dùng", "404005"));

        String adminCode = SecurityUtils.getCurrentStudentCode();

        if (user.getStudentCode().equals(adminCode)) {
            throw new BusinessException("Bạn không thể tự khóa tài khoản của chính mình", "400000");
        }

        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }
}