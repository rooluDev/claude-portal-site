package com.portfolio.auth.controller;

import com.portfolio.auth.dto.JoinRequestDto;
import com.portfolio.auth.dto.LoginRequestDto;
import com.portfolio.auth.dto.LoginResponseDto;
import com.portfolio.auth.service.AuthService;
import com.portfolio.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/join")
    public ResponseEntity<ApiResponse<Void>> join(@RequestBody @Valid JoinRequestDto dto) {
        authService.join(dto);
        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다.", null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@RequestBody @Valid LoginRequestDto dto) {
        LoginResponseDto response = authService.login(dto);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
