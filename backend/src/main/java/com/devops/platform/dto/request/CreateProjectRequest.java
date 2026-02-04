package com.devops.platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjectRequest {
    
    @NotBlank(message = "Название проекта обязательно")
    @Size(min = 2, max = 100, message = "Название должно быть от 2 до 100 символов")
    @Pattern(regexp = "^[a-zA-Z0-9-_]+$", message = "Название может содержать только буквы, цифры, дефисы и подчёркивания")
    private String name;
    
    @Size(max = 1000, message = "Описание не должно превышать 1000 символов")
    private String description;
    
    @NotNull(message = "Стек технологий обязателен")
    private StackRequest stack;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StackRequest {
        @NotBlank(message = "Backend технология обязательна")
        private String backend;
        
        @NotBlank(message = "Frontend технология обязательна")
        private String frontend;
        
        private String database = "postgres";
        
        private Boolean useDocker = true;
    }
}