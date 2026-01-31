package com.devops.platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    
    @NotBlank(message = "Текущий пароль обязателен")
    private String currentPassword;
    
    @NotBlank(message = "Новый пароль обязателен")
    @Size(min = 6, message = "Пароль должен быть минимум 6 символов")
    private String newPassword;
}