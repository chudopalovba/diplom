package com.devops.platform.service;

import com.devops.platform.config.GitLabConfig;
import com.devops.platform.dto.response.PipelineResponse;
import com.devops.platform.entity.Pipeline;
import com.devops.platform.entity.PipelineStage;
import com.devops.platform.entity.Project;
import com.devops.platform.entity.enums.PipelineStatus;
import com.devops.platform.entity.enums.ProjectStatus;
import com.devops.platform.repository.PipelineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PipelineService {
    
    private final PipelineRepository pipelineRepository;
    private final ProjectService projectService;
    private final GitLabService gitLabService;
    private final GitLabConfig gitLabConfig;
    
    public PipelineResponse getLatestPipeline(Long projectId) {
        Project project = projectService.findProjectByIdAndCurrentUser(projectId);
        
        return pipelineRepository.findTopByProjectOrderByStartedAtDesc(project)
                .map(this::enrichPipelineFromGitLab)
                .map(PipelineResponse::fromEntity)
                .orElse(null);
    }
    
    public List<PipelineResponse> getPipelineHistory(Long projectId) {
        Project project = projectService.findProjectByIdAndCurrentUser(projectId);
        
        return pipelineRepository.findByProjectWithStages(project)
                .stream()
                .map(PipelineResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public PipelineResponse triggerBuild(Long projectId) {
        Project project = projectService.findProjectByIdAndCurrentUser(projectId);
        
        log.info("Triggering build for project: {}", project.getName());
        
        Pipeline pipeline = createPipeline(project, List.of("build", "test"));
        
        // Запускаем пайплайн в GitLab
        if (project.getGitlabProjectId() != null) {
            try {
                var gitlabPipeline = gitLabService.triggerPipeline(
                        project.getGitlabProjectId(),
                        gitLabConfig.getDefaultBranch()
                );
                pipeline.setGitlabPipelineId(gitlabPipeline.id());
                pipeline.setStatus(PipelineStatus.RUNNING);
            } catch (Exception e) {
                log.error("Failed to trigger GitLab pipeline: {}", e.getMessage());
                pipeline.setStatus(PipelineStatus.FAILED);
            }
        } else {
            // Симуляция для тестирования без GitLab
            simulatePipeline(pipeline);
        }
        
        pipeline = pipelineRepository.save(pipeline);
        projectService.updateProjectStatus(projectId, ProjectStatus.DEVELOPING);
        
        return PipelineResponse.fromEntity(pipeline);
    }
    
    @Transactional
    public PipelineResponse triggerDeploy(Long projectId) {
        Project project = projectService.findProjectByIdAndCurrentUser(projectId);
        
        log.info("Triggering deploy for project: {}", project.getName());
        
        Pipeline pipeline = createPipeline(project, List.of("build", "test", "deploy"));
        
        if (project.getGitlabProjectId() != null) {
            try {
                var gitlabPipeline = gitLabService.triggerPipeline(
                        project.getGitlabProjectId(),
                        gitLabConfig.getDefaultBranch()
                );
                pipeline.setGitlabPipelineId(gitlabPipeline.id());
                pipeline.setStatus(PipelineStatus.RUNNING);
            } catch (Exception e) {
                log.error("Failed to trigger GitLab pipeline: {}", e.getMessage());
                pipeline.setStatus(PipelineStatus.FAILED);
            }
        } else {
            simulatePipeline(pipeline);
            // Симулируем успешный деплой
            String deployUrl = String.format("http://%s.apps.local", project.getName());
            pipeline.setDeployUrl(deployUrl);
            projectService.updateDeployUrl(projectId, deployUrl);
        }
        
        pipeline = pipelineRepository.save(pipeline);
        
        return PipelineResponse.fromEntity(pipeline);
    }
    
    @Transactional
    public PipelineResponse triggerSonarQube(Long projectId) {
        Project project = projectService.findProjectByIdAndCurrentUser(projectId);
        
        log.info("Triggering SonarQube analysis for project: {}", project.getName());
        
        Pipeline pipeline = createPipeline(project, List.of("build", "sonar"));
        
        if (project.getGitlabProjectId() != null) {
            try {
                var gitlabPipeline = gitLabService.triggerPipeline(
                        project.getGitlabProjectId(),
                        gitLabConfig.getDefaultBranch()
                );
                pipeline.setGitlabPipelineId(gitlabPipeline.id());
                pipeline.setStatus(PipelineStatus.RUNNING);
            } catch (Exception e) {
                log.error("Failed to trigger GitLab pipeline: {}", e.getMessage());
                pipeline.setStatus(PipelineStatus.FAILED);
            }
        } else {
            simulatePipeline(pipeline);
        }
        
        pipeline = pipelineRepository.save(pipeline);
        
        return PipelineResponse.fromEntity(pipeline);
    }
    
    private Pipeline createPipeline(Project project, List<String> stageNames) {
        Pipeline pipeline = Pipeline.builder()
                .project(project)
                .status(PipelineStatus.PENDING)
                .startedAt(LocalDateTime.now())
                .build();
        
        List<PipelineStage> stages = stageNames.stream()
                .map(name -> PipelineStage.builder()
                        .name(name)
                        .status(PipelineStatus.PENDING)
                        .pipeline(pipeline)
                        .build())
                .collect(Collectors.toList());
        
        pipeline.setStages(stages);
        return pipeline;
    }
    
    private void simulatePipeline(Pipeline pipeline) {
        // Симуляция успешного выполнения для тестирования
        pipeline.setStatus(PipelineStatus.SUCCESS);
        pipeline.setFinishedAt(LocalDateTime.now());
        
        for (PipelineStage stage : pipeline.getStages()) {
            stage.setStatus(PipelineStatus.SUCCESS);
        }
    }
    
    private Pipeline enrichPipelineFromGitLab(Pipeline pipeline) {
        if (pipeline.getGitlabPipelineId() != null && pipeline.getProject().getGitlabProjectId() != null) {
            try {
                var gitlabPipeline = gitLabService.getPipelineStatus(
                        pipeline.getProject().getGitlabProjectId(),
                        pipeline.getGitlabPipelineId()
                );
                
                if (gitlabPipeline != null) {
                    pipeline.setStatus(PipelineStatus.valueOf(gitlabPipeline.status().toUpperCase()));
                }
            } catch (Exception e) {
                log.warn("Failed to get GitLab pipeline status: {}", e.getMessage());
            }
        }
        return pipeline;
    }
}