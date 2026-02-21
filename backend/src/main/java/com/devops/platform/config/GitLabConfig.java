package com.devops.platform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class GitLabConfig {

    @Value("${gitlab.url}")
    private String gitlabUrl;

    @Value("${gitlab.external-url}")
    private String gitlabExternalUrl;

    @Value("${gitlab.token}")
    private String gitlabToken;

    @Value("${gitlab.group-id}")
    private Long gitlabGroupId;

    @Bean(name = "gitlabRestTemplate")
    public RestTemplate gitlabRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
    }

    public String getGitlabUrl() {
        return gitlabUrl;
    }

    public String getGitlabExternalUrl() {
        return gitlabExternalUrl;
    }

    public String getGitlabToken() {
        return gitlabToken;
    }

    public Long getGitlabGroupId() {
        return gitlabGroupId;
    }
}