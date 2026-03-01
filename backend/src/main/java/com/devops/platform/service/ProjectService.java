package com.devops.platform.service;

import com.devops.platform.dto.request.CreateProjectRequest;
import com.devops.platform.dto.response.ProjectResponse;
import com.devops.platform.entity.Project;
import com.devops.platform.entity.TechStack;
import com.devops.platform.entity.User;
import com.devops.platform.entity.enums.BackendTech;
import com.devops.platform.entity.enums.DatabaseTech;
import com.devops.platform.entity.enums.FrontendTech;
import com.devops.platform.entity.enums.ProjectStatus;
import com.devops.platform.exception.BadRequestException;
import com.devops.platform.exception.ResourceNotFoundException;
import com.devops.platform.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepository projectRepository;
    private final AuthService authService;
    private final GitLabService gitLabService;
    private final ProjectTemplateService projectTemplateService;

    public ProjectService(ProjectRepository projectRepository,
                          AuthService authService,
                          GitLabService gitLabService,
                          ProjectTemplateService projectTemplateService) {
        this.projectRepository = projectRepository;
        this.authService = authService;
        this.gitLabService = gitLabService;
        this.projectTemplateService = projectTemplateService;
    }

    // =========================================================================
    //  READ
    // =========================================================================

    public List<ProjectResponse> getCurrentUserProjects() {
        User user = authService.getCurrentUser();
        return projectRepository.findByOwnerOrderByCreatedAtDesc(user)
                .stream()
                .map(ProjectResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public ProjectResponse getProject(Long id) {
        Project project = findProjectByIdAndCurrentUser(id);
        return ProjectResponse.fromEntity(project);
    }

    // =========================================================================
    //  CREATE
    // =========================================================================

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        User user = authService.getCurrentUser();

        log.info("=== CREATE PROJECT '{}' for user '{}' ===",
                request.getName(), user.getRealUsername());

        // ── Валидация ───────────────────────────────────────────────
        if (projectRepository.existsByNameAndOwner(request.getName(), user)) {
            throw new BadRequestException("Проект с таким названием уже существует");
        }
        if (user.getGitlabUserId() == null) {
            throw new BadRequestException("GitLab-аккаунт не привязан. Обратитесь к администратору.");
        }

        // ── Стек ────────────────────────────────────────────────────
        TechStack stack = TechStack.builder()
                .backend(BackendTech.valueOf(
                        request.getStack().getBackend().toUpperCase()))
                .frontend(FrontendTech.valueOf(
                        request.getStack().getFrontend().toUpperCase()))
                .database(DatabaseTech.valueOf(
                        request.getStack().getDatabase().toUpperCase()))
                .useDocker(request.getStack().getUseDocker())
                .build();

        // ── Entity ──────────────────────────────────────────────────
        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .status(ProjectStatus.CREATED)
                .stack(stack)
                .owner(user)
                .build();

        project = projectRepository.save(project);

        try {
            // ── 1. Создаём репозиторий в GitLab ─────────────────────
            log.info("Step 1: Creating GitLab repository...");
            GitLabService.GitLabProjectInfo gitlabProject =
                    gitLabService.createProjectInGroup(
                            request.getName(),
                            user.getRealUsername(),
                            user.getGitlabUserId());

            project.setGitlabProjectId(gitlabProject.getId());
            project.setGitlabUrl(
                    gitLabService.getExternalProjectUrl(
                            gitlabProject.getPathWithNamespace()));
            project.setGitCloneUrl(
                    gitLabService.getExternalCloneUrl(
                            gitlabProject.getPathWithNamespace()));

            // ── 2. Генерируем файлы шаблона ─────────────────────────
            log.info("Step 2: Generating template files...");
            Map<String, String> files =
                    projectTemplateService.generateProjectFiles(
                            request.getName(), stack);

            // ── 3. Пушим файлы в репозиторий ────────────────────────
            log.info("Step 3: Pushing {} files to GitLab...", files.size());
            if (!files.isEmpty()) {
                String commitMsg = String.format(
                        "Initial project setup [%s + %s, docker=%s]",
                        stack.getBackend().name().toLowerCase(),
                        stack.getFrontend().name().toLowerCase(),
                        stack.getUseDocker());

                gitLabService.commitFiles(
                        gitlabProject.getId(), files, commitMsg);
            }

            project.setStatus(ProjectStatus.ACTIVE);
            log.info("=== PROJECT '{}' CREATED SUCCESSFULLY ===", request.getName());

        } catch (Exception e) {
            log.error("GitLab FAILED for '{}': {}", request.getName(), e.getMessage(), e);
            project.setStatus(ProjectStatus.FAILED);
            project = projectRepository.save(project);
            throw new RuntimeException("Ошибка создания репозитория: " + e.getMessage(), e);
        }

        project = projectRepository.save(project);
        return ProjectResponse.fromEntity(project);
    }

    // =========================================================================
    //  DELETE
    // =========================================================================

    @Transactional
    public void deleteProject(Long id) {
        Project project = findProjectByIdAndCurrentUser(id);
        log.info("Deleting project: {}", project.getName());

        if (project.getGitlabProjectId() != null) {
            try {
                gitLabService.deleteProject(project.getGitlabProjectId());
                log.info("GitLab repo deleted for '{}'", project.getName());
            } catch (Exception e) {
                log.warn("GitLab delete failed for '{}': {}", project.getName(), e.getMessage());
            }
        }

        projectRepository.delete(project);
    }

    // =========================================================================
    //  STATUS UPDATES
    // =========================================================================

    @Transactional
    public void updateProjectStatus(Long id, ProjectStatus status) {
        Project project = findProjectByIdAndCurrentUser(id);
        project.setStatus(status);
        projectRepository.save(project);
    }

    @Transactional
    public void updateDeployUrl(Long id, String deployUrl) {
        Project project = findProjectByIdAndCurrentUser(id);
        project.setDeployUrl(deployUrl);
        project.setStatus(ProjectStatus.DEPLOYED);
        projectRepository.save(project);
    }

    // =========================================================================
    //  HELPERS
    // =========================================================================

    public Project findProjectByIdAndCurrentUser(Long id) {
        User user = authService.getCurrentUser();
        return projectRepository.findByIdAndOwner(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Проект", id));
    }
}