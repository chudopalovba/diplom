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
import java.util.stream.Collectors;

@Service
public class ProjectService {
    
    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);
    
    private final ProjectRepository projectRepository;
    private final AuthService authService;
    
    public ProjectService(ProjectRepository projectRepository, AuthService authService) {
        this.projectRepository = projectRepository;
        this.authService = authService;
    }
    
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
        
        log.info("Creating project: {} for user: {}", request.getName(), user.getRealUsername());
        
        if (projectRepository.existsByNameAndOwner(request.getName(), user)) {
            throw new BadRequestException("Проект с таким названием уже существует");
        }
        
        TechStack stack = TechStack.builder()
                .backend(BackendTech.valueOf(request.getStack().getBackend().toUpperCase()))
                .frontend(FrontendTech.valueOf(request.getStack().getFrontend().toUpperCase()))
                .database(DatabaseTech.valueOf(request.getStack().getDatabase().toUpperCase()))
                .useDocker(request.getStack().getUseDocker())
                .build();
        
        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .status(ProjectStatus.CREATED)
                .stack(stack)
                .owner(user)
                .gitlabUrl("http://gitlab.local:8929/" + user.getRealUsername() + "/" + request.getName())
                .gitCloneUrl("git@gitlab.local:8929:" + user.getRealUsername() + "/" + request.getName() + ".git")
                .build();
        
        project = projectRepository.save(project);
        
        return ProjectResponse.fromEntity(project);
    }
    
    @Transactional
    public void deleteProject(Long id) {
        Project project = findProjectByIdAndCurrentUser(id);
        log.info("Deleting project: {}", project.getName());
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
}