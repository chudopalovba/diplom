package com.devops.platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String token;
    private UserResponse user;
    
    public static AuthResponse of(String token, UserResponse user) {
        return new AuthResponse(token, user);
    }
}