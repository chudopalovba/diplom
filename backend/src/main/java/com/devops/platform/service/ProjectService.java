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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {
    
    private final ProjectRepository projectRepository;
    private final AuthService authService;
    private final GitLabService gitLabService;
    private final CiCdFileService ciCdFileService;
    
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
    
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        User user = authService.getCurrentUser();
        
        log.info("Creating project: {} for user: {}", request.getName(), user.getUsername());
        
        if (projectRepository.existsByNameAndOwner(request.getName(), user)) {
            throw new BadRequestException("Проект с таким названием уже существует");
        }
        
        // Парсим стек
        TechStack stack = TechStack.builder()
                .backend(BackendTech.valueOf(request.getStack().getBackend().toUpperCase()))
                .frontend(FrontendTech.valueOf(request.getStack().getFrontend().toUpperCase()))
                .database(DatabaseTech.valueOf(request.getStack().getDatabase().toUpperCase()))
                .useDocker(request.getStack().getUseDocker())
                .build();
        
        // Создаём проект
        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .status(ProjectStatus.CREATED)
                .stack(stack)
                .owner(user)
                .build();
        
        // Создаём проект в GitLab
        try {
            var gitlabProject = gitLabService.createProject(
                    request.getName(),
                    user.getGitlabUserId(),
                    request.getDescription()
            );
            
            project.setGitlabProjectId(gitlabProject.id());
            project.setGitlabUrl(gitlabProject.webUrl());
            project.setGitCloneUrl(gitlabProject.httpUrlToRepo());
            
            log.info("GitLab project created: {}", gitlabProject.webUrl());
            
            // Добавляем .gitlab-ci.yml
            String ciContent = ciCdFileService.getGitLabCiContent(stack);
            gitLabService.addFile(
                    gitlabProject.id(),
                    ".gitlab-ci.yml",
                    ciContent,
                    "Add CI/CD configuration"
            );
            
            // Добавляем Dockerfile если нужно
            if (Boolean.TRUE.equals(stack.getUseDocker())) {
                String dockerfileContent = ciCdFileService.getDockerfileContent(stack);
                gitLabService.addFile(
                        gitlabProject.id(),
                        "Dockerfile",
                        dockerfileContent,
                        "Add Dockerfile"
                );
            }
            
        } catch (Exception e) {
            log.error("Failed to create GitLab project: {}", e.getMessage());
            // Сохраняем проект даже если GitLab недоступен
            project.setGitlabUrl("GitLab недоступен");
        }
        
        project = projectRepository.save(project);
        
        return ProjectResponse.fromEntity(project);
    }
    
    @Transactional
    public void deleteProject(Long id) {
        Project project = findProjectByIdAndCurrentUser(id);
        
        log.info("Deleting project: {}", project.getName());
        
        // Удаляем из GitLab
        if (project.getGitlabProjectId() != null) {
            gitLabService.deleteProject(project.getGitlabProjectId());
        }
        
        projectRepository.delete(project);
    }
    
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
    
    public Project findProjectByIdAndCurrentUser(Long id) {
        User user = authService.getCurrentUser();
        return projectRepository.findByIdAndOwner(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Проект", id));
    }
    
    public long countUserProjects() {
        User user = authService.getCurrentUser();
        return projectRepository.countByOwner(user);
    }
    
    public long countDeployedProjects() {
        User user = authService.getCurrentUser();
        return projectRepository.countDeployedByOwner(user);
    }
}