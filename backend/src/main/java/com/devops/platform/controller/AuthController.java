package com.devops.platform.controller;

import com.devops.platform.dto.request.ChangePasswordRequest;
import com.devops.platform.dto.request.LoginRequest;
import com.devops.platform.dto.request.RegisterRequest;
import com.devops.platform.dto.request.UpdateProfileRequest;
import com.devops.platform.dto.response.ApiResponse;
import com.devops.platform.dto.response.AuthResponse;
import com.devops.platform.dto.response.UserResponse;
import com.devops.platform.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        UserResponse user = UserResponse.fromEntity(authService.getCurrentUser());
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        UserResponse user = authService.updateProfile(request);
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success("Пароль успешно изменён"));
    }
}