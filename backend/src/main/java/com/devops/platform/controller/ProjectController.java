package com.devops.platform.controller;

import com.devops.platform.dto.request.CreateProjectRequest;
import com.devops.platform.dto.response.ApiResponse;
import com.devops.platform.dto.response.ProjectResponse;
import com.devops.platform.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    
    private final ProjectService projectService;
    
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }
    
    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getProjects() {
        List<ProjectResponse> projects = projectService.getCurrentUserProjects();
        return ResponseEntity.ok(projects);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProject(@PathVariable Long id) {
        ProjectResponse project = projectService.getProject(id);
        return ResponseEntity.ok(project);
    }
    
    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request) {
        ProjectResponse project = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(project);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.success("Проект удалён"));
    }
    
    @GetMapping("/{id}/gitlab")
    public ResponseEntity<Map<String, String>> getGitlabInfo(@PathVariable Long id) {
        ProjectResponse project = projectService.getProject(id);
        return ResponseEntity.ok(Map.of(
                "url", project.getGitlabUrl() != null ? project.getGitlabUrl() : "",
                "cloneUrl", project.getGitCloneUrl() != null ? project.getGitCloneUrl() : ""
        ));
    }
    
    @GetMapping("/{id}/stats")
    public ResponseEntity<Map<String, Object>> getProjectStats(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of(
                "builds", 10,
                "deploys", 5,
                "lastActivity", java.time.LocalDateTime.now().toString()
        ));
    }
}