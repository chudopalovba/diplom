package com.devops.platform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "gitlab")
public class GitLabConfig {
    
    private String url;
    private String token;
    private String defaultBranch = "main";
}