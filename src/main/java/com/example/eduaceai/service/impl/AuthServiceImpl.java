package com.example.eduaceai.service.impl;

import com.example.eduaceai.dto.req.LoginRequest;
import com.example.eduaceai.dto.req.RegisterRequest;
import com.example.eduaceai.dto.res.AuthResponse;
import com.example.eduaceai.entity.User;
import com.example.eduaceai.exception.BusinessException;
import com.example.eduaceai.repository.UserRepository;
import com.example.eduaceai.service.IAuthService;
import com.example.eduaceai.utils.JwtUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    @Transactional
    public User register(RegisterRequest req) {
        if (userRepository.existsByStudentCode(req.studentCode())) {
            throw new BusinessException("Mã sinh viên này đã tồn tại", "400019");
        }

        User user = User.builder()
                .studentCode(req.studentCode())
                .password(passwordEncoder.encode(req.password()))
                .fullName(req.fullName())
                .role(User.Role.STUDENT)
                .enabled(true)
                .build();

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByStudentCode(req.studentCode())
                .orElseThrow(() -> new BusinessException("Mã sinh viên hoặc mật khẩu không đúng", "400021"));

        if (!user.isEnabled()) {
            throw new BusinessException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ Admin!", "403002");
        }

        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new BusinessException("Mã sinh viên hoặc mật khẩu không đúng", "400021");
        }

        String token = jwtUtils.generateToken(user.getStudentCode());

        return new AuthResponse(token, user.getStudentCode(), user.getFullName(), user.getRole().name());
    }
}
