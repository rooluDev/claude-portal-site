package com.portfolio.auth.dto;

import com.portfolio.user.entity.User;
import lombok.Getter;

@Getter
public class LoginResponseDto {

    private final String token;
    private final UserInfo user;

    public LoginResponseDto(String token, User user) {
        this.token = token;
        this.user = new UserInfo(user);
    }

    @Getter
    public static class UserInfo {
        private final Long id;
        private final String username;
        private final String name;
        private final String role;

        public UserInfo(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.name = user.getName();
            this.role = user.getRole().name();
        }
    }
}
