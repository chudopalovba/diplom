package com.devops.platform.dto.response;

import com.devops.platform.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    
    private Long id;
    private String name;
    private String description;
    private String status;
    private StackResponse stack;
    private String gitlabUrl;
    private String gitCloneUrl;
    private String deployUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StackResponse {
        private String backend;
        private String frontend;
        private String database;
        private Boolean useDocker;
    }
    
    public static ProjectResponse fromEntity(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setName(project.getName());
        response.setDescription(project.getDescription());
        response.setStatus(project.getStatus().name().toLowerCase());
        
        if (project.getStack() != null) {
            StackResponse stackResponse = new StackResponse();
            stackResponse.setBackend(project.getStack().getBackend().name().toLowerCase());
            stackResponse.setFrontend(project.getStack().getFrontend().name().toLowerCase());
            stackResponse.setDatabase(project.getStack().getDatabase().name().toLowerCase());
            stackResponse.setUseDocker(project.getStack().getUseDocker());
            response.setStack(stackResponse);
        }
        
        response.setGitlabUrl(project.getGitlabUrl());
        response.setGitCloneUrl(project.getGitCloneUrl());
        response.setDeployUrl(project.getDeployUrl());
        response.setCreatedAt(project.getCreatedAt());
        response.setUpdatedAt(project.getUpdatedAt());
        return response;
    }
}