package com.devops.platform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class GitLabConfig {

    /**
     * Внутренний URL GitLab (для API-вызовов из backend).
     * В docker-сети: http://gitlab (порт 80)
     * Локально: http://localhost:8929
     */
    @Value("${gitlab.url}")
    private String gitlabUrl;

    /**
     * Внешний URL GitLab (для ссылок, которые увидит пользователь в браузере).
     * Всегда: http://localhost:8929
     */
    @Value("${gitlab.external-url}")
    private String gitlabExternalUrl;

    /**
     * Admin Personal Access Token (api + read_user + sudo).
     */
    @Value("${gitlab.token}")
    private String gitlabToken;

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
}