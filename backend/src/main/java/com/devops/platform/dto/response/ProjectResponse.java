package com.devops.platform.dto.response;

import com.devops.platform.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
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
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StackResponse {
        private String backend;
        private String frontend;
        private String database;
        private Boolean useDocker;
    }
    
    public static ProjectResponse fromEntity(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus().name().toLowerCase())
                .stack(StackResponse.builder()
                        .backend(project.getStack().getBackend().name().toLowerCase())
                        .frontend(project.getStack().getFrontend().name().toLowerCase())
                        .database(project.getStack().getDatabase().name().toLowerCase())
                        .useDocker(project.getStack().getUseDocker())
                        .build())
                .gitlabUrl(project.getGitlabUrl())
                .gitCloneUrl(project.getGitCloneUrl())
                .deployUrl(project.getDeployUrl())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}