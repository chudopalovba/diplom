package com.devops.platform.service;

import com.devops.platform.config.GitLabConfig;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class GitLabService {

    private static final Logger log = LoggerFactory.getLogger(GitLabService.class);

    private final RestTemplate restTemplate;
    private final GitLabConfig gitLabConfig;

    public GitLabService(@Qualifier("gitlabRestTemplate") RestTemplate restTemplate,
                         GitLabConfig gitLabConfig) {
        this.restTemplate = restTemplate;
        this.gitLabConfig = gitLabConfig;
    }

    // =========================================================================
    //  USER OPERATIONS
    // =========================================================================

    /**
     * Создать пользователя в GitLab с такими же email и паролем.
     * Admin API: POST /api/v4/users
     */
    public GitLabUserInfo createUser(String email, String username,
                                     String password, String name) {
        String url = apiUrl("/api/v4/users");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("email", email);
        body.put("password", password);
        body.put("username", sanitizeUsername(username));
        body.put("name", (name != null && !name.isBlank()) ? name : username);
        body.put("skip_confirmation", true);   // без подтверждения email
        body.put("force_random_password", false);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, authHeaders());

        try {
            ResponseEntity<GitLabUserInfo> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, GitLabUserInfo.class);

            GitLabUserInfo user = response.getBody();
            log.info("GitLab user CREATED: id={}, username={}, email={}",
                    user.getId(), user.getUsername(), user.getEmail());
            return user;

        } catch (HttpClientErrorException.Conflict e) {
            log.warn("GitLab user conflict (email={}), searching existing", email);
            return findUserByEmail(email)
                    .orElseThrow(() -> new RuntimeException(
                            "GitLab user conflict, cannot find by email: " + email));

        } catch (HttpClientErrorException e) {
            log.error("GitLab create user FAILED: {} — {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException(
                    "Ошибка создания пользователя GitLab: " + e.getMessage(), e);
        }
    }

    /**
     * Найти пользователя по email
     */
    public Optional<GitLabUserInfo> findUserByEmail(String email) {
        String url = apiUrl("/api/v4/users?search=" + email);
        HttpEntity<?> request = new HttpEntity<>(authHeaders());

        try {
            ResponseEntity<List<GitLabUserInfo>> response = restTemplate.exchange(
                    url, HttpMethod.GET, request,
                    new ParameterizedTypeReference<List<GitLabUserInfo>>() {});

            List<GitLabUserInfo> users = response.getBody();
            if (users != null) {
                return users.stream()
                        .filter(u -> email.equalsIgnoreCase(u.getEmail()))
                        .findFirst();
            }
        } catch (Exception e) {
            log.error("Find user by email failed: {}", e.getMessage());
        }
        return Optional.empty();
    }

    // =========================================================================
    //  PROJECT OPERATIONS
    // =========================================================================

    /**
     * Создать проект в namespace пользователя.
     * Admin API: POST /api/v4/projects/user/:user_id
     */
    public GitLabProjectInfo createProject(String projectName,
                                           Long gitlabUserId) {
        String url = apiUrl("/api/v4/projects/user/" + gitlabUserId);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", projectName);
        body.put("path", sanitizeProjectPath(projectName));
        body.put("visibility", "private");
        body.put("initialize_with_readme", false);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, authHeaders());

        try {
            ResponseEntity<GitLabProjectInfo> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, GitLabProjectInfo.class);

            GitLabProjectInfo project = response.getBody();
            log.info("GitLab project CREATED: id={}, path={}",
                    project.getId(), project.getPathWithNamespace());
            return project;

        } catch (HttpClientErrorException e) {
            log.error("GitLab create project FAILED: {} — {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException(
                    "Ошибка создания проекта GitLab: " + e.getMessage(), e);
        }
    }

    /**
     * Удалить проект в GitLab.
     * DELETE /api/v4/projects/:id
     */
    public void deleteProject(Long gitlabProjectId) {
        if (gitlabProjectId == null) return;

        String url = apiUrl("/api/v4/projects/" + gitlabProjectId);
        HttpEntity<?> request = new HttpEntity<>(authHeaders());

        try {
            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
            log.info("GitLab project DELETED: id={}", gitlabProjectId);
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("GitLab project {} not found, skip delete", gitlabProjectId);
        } catch (HttpClientErrorException e) {
            log.error("GitLab delete project FAILED: {}", e.getMessage());
        }
    }

    // =========================================================================
    //  COMMIT FILES TO REPO
    // =========================================================================

    /**
     * Запушить файлы одним коммитом через Commits API.
     * POST /api/v4/projects/:id/repository/commits
     */
    public void commitFiles(Long gitlabProjectId,
                            Map<String, String> files,
                            String commitMessage) {

        String url = apiUrl("/api/v4/projects/" + gitlabProjectId
                + "/repository/commits");

        List<Map<String, String>> actions = new ArrayList<>();
        for (Map.Entry<String, String> entry : files.entrySet()) {
            Map<String, String> action = new LinkedHashMap<>();
            action.put("action", "create");
            action.put("file_path", entry.getKey());
            action.put("content", entry.getValue());
            actions.add(action);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("branch", "main");
        body.put("commit_message", commitMessage);
        body.put("actions", actions);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, authHeaders());

        try {
            restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            log.info("Committed {} files to project {}",
                    files.size(), gitlabProjectId);
        } catch (HttpClientErrorException e) {
            log.error("Commit FAILED: {} — {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException(
                    "Ошибка push файлов: " + e.getMessage(), e);
        }
    }

    /**
     * Собрать шаблонные файлы и запушить в репозиторий.
     */
    public void pushProjectTemplate(Long gitlabProjectId,
                                    String projectName,
                                    String backendTech,
                                    String frontendTech,
                                    String databaseTech,
                                    boolean useDocker) {

        Map<String, String> files = new LinkedHashMap<>();

        // ── .gitlab-ci.yml ────────────────────────────────────────
        String ciFileName = buildCiFileName(backendTech, useDocker);
        String ciContent = readResource("templates/gitlab-ci/" + ciFileName);
        if (ciContent != null) {
            files.put(".gitlab-ci.yml", ciContent);
        } else {
            log.warn("CI template not found: {}", ciFileName);
        }

        // ── .gitignore ───────────────────────────────────────────
        String gitignore = readResource("templates/gitignore.txt");
        if (gitignore != null) {
            files.put(".gitignore", gitignore);
        }

        // ── README.md ────────────────────────────────────────────
        String readme = readResource("templates/README.md");
        if (readme != null) {
            readme = replacePlaceholders(readme, projectName,
                    backendTech, frontendTech);
            files.put("README.md", readme);
        }

        // ── Backend files ────────────────────────────────────────
        files.putAll(collectBackendFiles(backendTech, projectName));

        // ── Frontend files ───────────────────────────────────────
        files.putAll(collectFrontendFiles(frontendTech, projectName));

        // ── Commit ───────────────────────────────────────────────
        if (!files.isEmpty()) {
            String msg = String.format(
                    "Initial project setup [%s + %s, docker=%s]",
                    backendTech, frontendTech, useDocker);
            commitFiles(gitlabProjectId, files, msg);
            log.info("Pushed {} template files for '{}'",
                    files.size(), projectName);
        } else {
            log.warn("No template files collected for '{}'", projectName);
        }
    }

    // =========================================================================
    //  PIPELINE OPERATIONS
    // =========================================================================

    /**
     * POST /api/v4/projects/:id/pipeline
     */
    public GitLabPipelineInfo triggerPipeline(Long gitlabProjectId,
                                             String ref) {
        String url = apiUrl("/api/v4/projects/" + gitlabProjectId
                + "/pipeline");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ref", ref != null ? ref : "main");

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, authHeaders());

        try {
            ResponseEntity<GitLabPipelineInfo> response =
                    restTemplate.exchange(url, HttpMethod.POST, request,
                            GitLabPipelineInfo.class);
            GitLabPipelineInfo pipeline = response.getBody();
            log.info("Pipeline triggered: project={}, pipeline={}",
                    gitlabProjectId, pipeline.getId());
            return pipeline;
        } catch (HttpClientErrorException e) {
            log.error("Trigger pipeline FAILED: {}", e.getMessage());
            throw new RuntimeException(
                    "Ошибка запуска пайплайна: " + e.getMessage(), e);
        }
    }

    /**
     * GET /api/v4/projects/:id/pipelines/:pipeline_id
     */
    public GitLabPipelineInfo getPipelineStatus(Long gitlabProjectId,
                                                Long pipelineId) {
        String url = apiUrl("/api/v4/projects/" + gitlabProjectId
                + "/pipelines/" + pipelineId);
        HttpEntity<?> request = new HttpEntity<>(authHeaders());

        ResponseEntity<GitLabPipelineInfo> response =
                restTemplate.exchange(url, HttpMethod.GET, request,
                        GitLabPipelineInfo.class);
        return response.getBody();
    }

    /**
     * GET /api/v4/projects/:id/pipelines
     */
    public List<GitLabPipelineInfo> listPipelines(Long gitlabProjectId) {
        String url = apiUrl("/api/v4/projects/" + gitlabProjectId
                + "/pipelines?per_page=20&order_by=id&sort=desc");
        HttpEntity<?> request = new HttpEntity<>(authHeaders());

        ResponseEntity<List<GitLabPipelineInfo>> response =
                restTemplate.exchange(url, HttpMethod.GET, request,
                        new ParameterizedTypeReference<
                                List<GitLabPipelineInfo>>() {});
        return response.getBody();
    }

    /**
     * GET /api/v4/projects/:id/pipelines/:pipeline_id/jobs
     */
    public List<GitLabJobInfo> getPipelineJobs(Long gitlabProjectId,
                                               Long pipelineId) {
        String url = apiUrl("/api/v4/projects/" + gitlabProjectId
                + "/pipelines/" + pipelineId + "/jobs");
        HttpEntity<?> request = new HttpEntity<>(authHeaders());

        ResponseEntity<List<GitLabJobInfo>> response =
                restTemplate.exchange(url, HttpMethod.GET, request,
                        new ParameterizedTypeReference<
                                List<GitLabJobInfo>>() {});
        return response.getBody();
    }

    // =========================================================================
    //  URL HELPERS
    // =========================================================================

    /**
     * http://localhost:8929/username
     */
    public String getExternalProfileUrl(String username) {
        return gitLabConfig.getGitlabExternalUrl()
                + "/" + sanitizeUsername(username);
    }

    /**
     * http://localhost:8929/username/project-name
     */
    public String getExternalProjectUrl(String pathWithNamespace) {
        return gitLabConfig.getGitlabExternalUrl()
                + "/" + pathWithNamespace;
    }

    /**
     * http://localhost:8929/username/project-name.git
     */
    public String getExternalCloneUrl(String pathWithNamespace) {
        return gitLabConfig.getGitlabExternalUrl()
                + "/" + pathWithNamespace + ".git";
    }

    // =========================================================================
    //  TEMPLATE COLLECTION — BACKEND
    // =========================================================================

    private Map<String, String> collectBackendFiles(String backendTech,
                                                    String projectName) {
        Map<String, String> files = new LinkedHashMap<>();
        String tech = backendTech.toLowerCase();
        String base = "templates/backend/" + tech + "/";

        switch (tech) {
            case "java" -> {
                String pkg = projectName.toLowerCase()
                        .replaceAll("[^a-z0-9]", "");

                addFile(files, base + "pom.xml",
                        "backend/pom.xml", projectName);
                addFile(files, base + "Application.java",
                        "backend/src/main/java/com/example/"
                                + pkg + "/Application.java",
                        projectName);
                addFile(files, base + "HelloController.java",
                        "backend/src/main/java/com/example/"
                                + pkg + "/controller/HelloController.java",
                        projectName);
                addFile(files, base + "application.yml",
                        "backend/src/main/resources/application.yml",
                        projectName);
                addFile(files, base + "Dockerfile",
                        "backend/Dockerfile", projectName);
            }
            case "python" -> {
                String pyPkg = projectName.toLowerCase()
                        .replaceAll("[^a-z0-9_]", "_");

                addFile(files, base + "settings.py",
                        "backend/" + pyPkg + "/settings.py",
                        projectName);
                addFile(files, base + "urls.py",
                        "backend/" + pyPkg + "/urls.py",
                        projectName);
                addFile(files, base + "api_urls.py",
                        "backend/api/urls.py", projectName);
                addFile(files, base + "views.py",
                        "backend/api/views.py", projectName);
                addFile(files, base + "Dockerfile",
                        "backend/Dockerfile", projectName);
            }
            case "csharp" -> {
                log.warn("C# templates not yet implemented");
            }
            default -> log.warn("Unknown backend: {}", tech);
        }
        return files;
    }

    // =========================================================================
    //  TEMPLATE COLLECTION — FRONTEND
    // =========================================================================

    private Map<String, String> collectFrontendFiles(String frontendTech,
                                                     String projectName) {
        Map<String, String> files = new LinkedHashMap<>();
        String tech = frontendTech.toLowerCase();
        String base = "templates/frontend/" + tech + "/";

        switch (tech) {
            case "react" -> {
                addFile(files, base + "vite.config.js",
                        "frontend/vite.config.js", projectName);
                addFile(files, base + "index.html",
                        "frontend/index.html", projectName);
                addFile(files, base + "main.jsx",
                        "frontend/src/main.jsx", projectName);
                addFile(files, base + "App.jsx",
                        "frontend/src/App.jsx", projectName);
                addFile(files, base + "index.css",
                        "frontend/src/index.css", projectName);
                addFile(files, base + "nginx.conf",
                        "frontend/nginx.conf", projectName);
                addFile(files, base + "Dockerfile",
                        "frontend/Dockerfile", projectName);
            }
            case "vue" -> log.warn("Vue templates not yet implemented");
            case "angular" -> log.warn("Angular templates not yet implemented");
            default -> log.warn("Unknown frontend: {}", tech);
        }
        return files;
    }

    // =========================================================================
    //  PRIVATE HELPERS
    // =========================================================================

    private void addFile(Map<String, String> files,
                         String resourcePath, String repoPath,
                         String projectName) {
        String content = readResource(resourcePath);
        if (content != null) {
            content = replacePlaceholders(content, projectName,
                    null, null);
            files.put(repoPath, content);
        }
    }

    private String replacePlaceholders(String content,
                                       String projectName,
                                       String backendTech,
                                       String frontendTech) {
        if (content == null) return null;

        content = content.replace("{{PROJECT_NAME}}", projectName);
        content = content.replace("{{project_name}}",
                projectName.toLowerCase()
                        .replaceAll("[^a-z0-9]", ""));
        content = content.replace("{{project-name}}",
                projectName.toLowerCase()
                        .replaceAll("[^a-z0-9-]", "-"));

        if (backendTech != null)
            content = content.replace("{{BACKEND_TECH}}", backendTech);
        if (frontendTech != null)
            content = content.replace("{{FRONTEND_TECH}}", frontendTech);

        return content;
    }

    /**
     * java-docker.yml / python-no-docker.yml / csharp-docker.yml
     */
    private String buildCiFileName(String backendTech, boolean useDocker) {
        return backendTech.toLowerCase()
                + (useDocker ? "-docker" : "-no-docker")
                + ".yml";
    }

    private String readResource(String path) {
        try {
            ClassPathResource res = new ClassPathResource(path);
            InputStream is = res.getInputStream();
            return StreamUtils.copyToString(is, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("Resource not found: {}", path);
            return null;
        }
    }

    private String apiUrl(String path) {
        String base = gitLabConfig.getGitlabUrl();
        if (base.endsWith("/"))
            base = base.substring(0, base.length() - 1);
        return base + path;
    }

    private HttpHeaders authHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set("PRIVATE-TOKEN", gitLabConfig.getGitlabToken());
        return h;
    }

    /**
     * GitLab username: [a-zA-Z0-9_.-], начинается с буквы/цифры
     */
    private String sanitizeUsername(String username) {
        String s = username
                .replaceAll("[^a-zA-Z0-9_.-]", "_")
                .toLowerCase();
        if (s.isEmpty() || !Character.isLetterOrDigit(s.charAt(0)))
            s = "u" + s;
        return s;
    }

    /**
     * GitLab project path: [a-z0-9-]
     */
    private String sanitizeProjectPath(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9-]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    // =========================================================================
    //  INNER DTOs (ответы GitLab API)
    // =========================================================================

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GitLabUserInfo {
        private Long id;
        private String username;
        private String email;
        private String name;
        private String state;
        @JsonProperty("web_url")
        private String webUrl;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GitLabProjectInfo {
        private Long id;
        private String name;
        private String path;
        @JsonProperty("path_with_namespace")
        private String pathWithNamespace;
        @JsonProperty("web_url")
        private String webUrl;
        @JsonProperty("http_url_to_repo")
        private String httpUrlToRepo;
        @JsonProperty("ssh_url_to_repo")
        private String sshUrlToRepo;
        @JsonProperty("default_branch")
        private String defaultBranch;
        private String visibility;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GitLabPipelineInfo {
        private Long id;
        private String status;
        private String ref;
        @JsonProperty("web_url")
        private String webUrl;
        @JsonProperty("created_at")
        private String createdAt;
        @JsonProperty("updated_at")
        private String updatedAt;
        @JsonProperty("started_at")
        private String startedAt;
        @JsonProperty("finished_at")
        private String finishedAt;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GitLabJobInfo {
        private Long id;
        private String name;
        private String stage;
        private String status;
        @JsonProperty("web_url")
        private String webUrl;
        @JsonProperty("created_at")
        private String createdAt;
        @JsonProperty("started_at")
        private String startedAt;
        @JsonProperty("finished_at")
        private String finishedAt;
    }
}