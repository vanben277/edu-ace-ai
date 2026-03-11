package com.example.eduaceai.security;

import com.example.eduaceai.entity.User;
import com.example.eduaceai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String studentCode) throws UsernameNotFoundException {
        User user = userRepository.findByStudentCode(studentCode)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getStudentCode())
                .password(user.getPassword())
                .disabled(!user.isEnabled())
                .authorities("ROLE_" + user.getRole().name())
                .build();
    }
}