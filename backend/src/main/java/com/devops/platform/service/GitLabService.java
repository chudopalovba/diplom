package com.devops.platform.service;

import com.devops.platform.config.GitLabConfig;
import com.devops.platform.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitLabService {
    
    private final GitLabConfig gitLabConfig;
    private WebClient webClient;
    
    private WebClient getWebClient() {
        if (webClient == null) {
            webClient = WebClient.builder()
                    .baseUrl(gitLabConfig.getUrl() + "/api/v4")
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeader("PRIVATE-TOKEN", gitLabConfig.getToken())
                    .build();
        }
        return webClient;
    }
    
    // DTO для GitLab ответов
    public record GitLabUser(Long id, String username, String email) {}
    public record GitLabProject(Long id, String name, String webUrl, String httpUrlToRepo, String sshUrlToRepo) {}
    public record GitLabPipeline(Long id, String status, String webUrl) {}
    
    /**
     * Создание пользователя в GitLab
     */
    public GitLabUser createUser(String username, String email, String password) {
        log.info("Creating GitLab user: {}", username);
        
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("username", username);
        body.put("name", username);
        body.put("password", password);
        body.put("skip_confirmation", true);
        
        try {
            return getWebClient()
                    .post()
                    .uri("/users")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(GitLabUser.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("GitLab API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BadRequestException("Ошибка создания пользователя в GitLab: " + e.getMessage());
        }
    }
    
    /**
     * Создание проекта в GitLab
     */
    public GitLabProject createProject(String projectName, Long userId, String description) {
        log.info("Creating GitLab project: {} for user: {}", projectName, userId);
        
        Map<String, Object> body = new HashMap<>();
        body.put("name", projectName);
        body.put("path", projectName.toLowerCase().replaceAll("[^a-z0-9-]", "-"));
        body.put("description", description);
        body.put("visibility", "private");
        body.put("initialize_with_readme", true);
        
        if (userId != null) {
            body.put("user_id", userId);
        }
        
        try {
            String uri = userId != null ? "/projects/user/" + userId : "/projects";
            
            return getWebClient()
                    .post()
                    .uri(uri)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(GitLabProject.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("GitLab API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BadRequestException("Ошибка создания проекта в GitLab: " + e.getMessage());
        }
    }
    
    /**
     * Добавление файла в репозиторий
     */
    public void addFile(Long projectId, String filePath, String content, String commitMessage) {
        log.info("Adding file to project {}: {}", projectId, filePath);
        
        Map<String, Object> body = new HashMap<>();
        body.put("branch", gitLabConfig.getDefaultBranch());
        body.put("content", content);
        body.put("commit_message", commitMessage);
        
        try {
            getWebClient()
                    .post()
                    .uri("/projects/{projectId}/repository/files/{filePath}", projectId, filePath)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("GitLab API error adding file: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BadRequestException("Ошибка добавления файла: " + e.getMessage());
        }
    }
    
    /**
     * Запуск пайплайна
     */
    public GitLabPipeline triggerPipeline(Long projectId, String ref) {
        log.info("Triggering pipeline for project: {} on ref: {}", projectId, ref);
        
        Map<String, Object> body = new HashMap<>();
        body.put("ref", ref);
        
        try {
            return getWebClient()
                    .post()
                    .uri("/projects/{projectId}/pipeline", projectId)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(GitLabPipeline.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("GitLab API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BadRequestException("Ошибка запуска пайплайна: " + e.getMessage());
        }
    }
    
    /**
     * Получение статуса пайплайна
     */
    public GitLabPipeline getPipelineStatus(Long projectId, Long pipelineId) {
        try {
            return getWebClient()
                    .get()
                    .uri("/projects/{projectId}/pipelines/{pipelineId}", projectId, pipelineId)
                    .retrieve()
                    .bodyToMono(GitLabPipeline.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("GitLab API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        }
    }
    
    /**
     * Удаление проекта
     */
    public void deleteProject(Long projectId) {
        log.info("Deleting GitLab project: {}", projectId);
        
        try {
            getWebClient()
                    .delete()
                    .uri("/projects/{projectId}", projectId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.warn("Failed to delete GitLab project: {}", e.getMessage());
        }
    }
}