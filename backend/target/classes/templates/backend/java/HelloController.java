package com.{{PACKAGE_NAME}}.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class HelloController {
    
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "{{PROJECT_NAME}}");
    }
    
    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of("message", "Hello from {{PROJECT_NAME}}!");
    }
}