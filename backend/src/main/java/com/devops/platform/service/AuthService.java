package com.devops.platform.service;

import com.devops.platform.dto.request.ChangePasswordRequest;
import com.devops.platform.dto.request.LoginRequest;
import com.devops.platform.dto.request.RegisterRequest;
import com.devops.platform.dto.request.UpdateProfileRequest;
import com.devops.platform.dto.response.AuthResponse;
import com.devops.platform.dto.response.UserResponse;
import com.devops.platform.entity.User;
import com.devops.platform.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final GitLabService gitLabService;
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getEmail());
        
        if (userService.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email уже используется");
        }
        
        if (userService.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Имя пользователя уже занято");
        }
        
        // Создаём пользователя в GitLab
        Long gitlabUserId = null;
        String gitlabUsername = null;
        try {
            var gitlabUser = gitLabService.createUser(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword()
            );
            gitlabUserId = gitlabUser.getId();
            gitlabUsername = gitlabUser.getUsername();
            log.info("GitLab user created: {}", gitlabUsername);
        } catch (Exception e) {
            log.warn("Failed to create GitLab user: {}", e.getMessage());
            // Продолжаем без GitLab пользователя
        }
        
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .gitlabUserId(gitlabUserId)
                .gitlabUsername(gitlabUsername)
                .build();
        
        user = userService.save(user);
        
        String token = jwtService.generateToken(user);
        
        return AuthResponse.builder()
                .token(token)
                .user(UserResponse.fromEntity(user))
                .build();
    }
    
    public AuthResponse login(LoginRequest request) {
        log.info("User login attempt: {}", request.getEmail());
        
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        
        User user = userService.findByEmail(request.getEmail());
        String token = jwtService.generateToken(user);
        
        return AuthResponse.builder()
                .token(token)
                .user(UserResponse.fromEntity(user))
                .build();
    }
    
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.findByEmail(email);
    }
    
    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        User user = getCurrentUser();
        
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userService.existsByUsername(request.getUsername())) {
                throw new BadRequestException("Имя пользователя уже занято");
            }
            user.setUsername(request.getUsername());
        }
        
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userService.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email уже используется");
            }
            user.setEmail(request.getEmail());
        }
        
        user = userService.save(user);
        return UserResponse.fromEntity(user);
    }
    
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = getCurrentUser();
        
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Неверный текущий пароль");
        }
        
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userService.save(user);
    }
}