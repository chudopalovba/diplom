package com.devops.platform.service;

import com.devops.platform.dto.response.PipelineResponse;
import com.devops.platform.entity.Pipeline;
import com.devops.platform.entity.PipelineStage;
import com.devops.platform.entity.Project;
import com.devops.platform.entity.enums.PipelineStatus;
import com.devops.platform.entity.enums.ProjectStatus;
import com.devops.platform.repository.PipelineRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PipelineService {
    
    private static final Logger log = LoggerFactory.getLogger(PipelineService.class);
    
    private final PipelineRepository pipelineRepository;
    private final ProjectService projectService;
    
    public PipelineService(PipelineRepository pipelineRepository, ProjectService projectService) {
        this.pipelineRepository = pipelineRepository;
        this.projectService = projectService;
    }
    
    public PipelineResponse getLatestPipeline(Long projectId) {
        Project project = projectService.findProjectByIdAndCurrentUser(projectId);
        
        return pipelineRepository.findTopByProjectOrderByStartedAtDesc(project)
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
        simulatePipeline(pipeline);
        
        pipeline = pipelineRepository.save(pipeline);
        projectService.updateProjectStatus(projectId, ProjectStatus.DEVELOPING);
        
        return PipelineResponse.fromEntity(pipeline);
    }
    
    @Transactional
    public PipelineResponse triggerDeploy(Long projectId) {
        Project project = projectService.findProjectByIdAndCurrentUser(projectId);
        
        log.info("Triggering deploy for project: {}", project.getName());
        
        Pipeline pipeline = createPipeline(project, List.of("build", "test", "deploy"));
        simulatePipeline(pipeline);
        
        String deployUrl = String.format("http://%s.apps.local", project.getName());
        pipeline.setDeployUrl(deployUrl);
        
        pipeline = pipelineRepository.save(pipeline);
        projectService.updateDeployUrl(projectId, deployUrl);
        
        return PipelineResponse.fromEntity(pipeline);
    }
    
    @Transactional
    public PipelineResponse triggerSonarQube(Long projectId) {
        Project project = projectService.findProjectByIdAndCurrentUser(projectId);
        
        log.info("Triggering SonarQube analysis for project: {}", project.getName());
        
        Pipeline pipeline = createPipeline(project, List.of("build", "sonar"));
        simulatePipeline(pipeline);
        
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
        pipeline.setStatus(PipelineStatus.SUCCESS);
        pipeline.setFinishedAt(LocalDateTime.now());
        
        for (PipelineStage stage : pipeline.getStages()) {
            stage.setStatus(PipelineStatus.SUCCESS);
        }
    }
}