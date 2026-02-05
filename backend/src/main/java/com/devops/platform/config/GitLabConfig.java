package com.devops.platform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "gitlab")
public class GitLabConfig {
    
    private String url = "http://gitlab.local:8929/";
    private String token = "glpat-4p7i43XqAo_LxF_VLf9A";
    private String defaultBranch = "main";
}