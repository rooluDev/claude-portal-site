package com.portfolio.auth.service;

import com.portfolio.auth.dto.JoinRequestDto;
import com.portfolio.auth.dto.LoginRequestDto;
import com.portfolio.auth.dto.LoginResponseDto;
import com.portfolio.auth.jwt.JwtProvider;
import com.portfolio.common.exception.CustomException;
import com.portfolio.common.exception.ErrorCode;
import com.portfolio.user.entity.User;
import com.portfolio.user.enums.Role;
import com.portfolio.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public void join(JoinRequestDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new CustomException(ErrorCode.DUPLICATE_USERNAME);
        }

        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .role(Role.USER)
                .build();

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginRequestDto dto) {
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_FAILED));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.LOGIN_FAILED);
        }

        String token = jwtProvider.generateToken(user);
        return new LoginResponseDto(token, user);
    }
}
