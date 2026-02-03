package org.example.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private long expiresIn;
    private UserInfo userInfo;

    @Data
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String displayName;
        private Integer userType;
        private List<String> authorities;
    }
}





