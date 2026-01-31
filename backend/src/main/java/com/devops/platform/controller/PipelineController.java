package com.devops.platform.controller;

import com.devops.platform.dto.response.ApiResponse;
import com.devops.platform.dto.response.PipelineResponse;
import com.devops.platform.service.PipelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects/{projectId}")
@RequiredArgsConstructor
public class PipelineController {
    
    private final PipelineService pipelineService;
    
    @GetMapping("/pipeline")
    public ResponseEntity<PipelineResponse> getPipelineStatus(@PathVariable Long projectId) {
        PipelineResponse pipeline = pipelineService.getLatestPipeline(projectId);
        return ResponseEntity.ok(pipeline);
    }
    
    @GetMapping("/pipeline/history")
    public ResponseEntity<List<PipelineResponse>> getPipelineHistory(@PathVariable Long projectId) {
        List<PipelineResponse> history = pipelineService.getPipelineHistory(projectId);
        return ResponseEntity.ok(history);
    }
    
    @PostMapping("/build")
    public ResponseEntity<ApiResponse<PipelineResponse>> triggerBuild(@PathVariable Long projectId) {
        PipelineResponse pipeline = pipelineService.triggerBuild(projectId);
        return ResponseEntity.ok(ApiResponse.success("Сборка запущена", pipeline));
    }
    
    @PostMapping("/deploy")
    public ResponseEntity<ApiResponse<PipelineResponse>> triggerDeploy(@PathVariable Long projectId) {
        PipelineResponse pipeline = pipelineService.triggerDeploy(projectId);
        return ResponseEntity.ok(ApiResponse.success("Деплой запущен", pipeline));
    }
    
    @PostMapping("/sonar")
    public ResponseEntity<ApiResponse<PipelineResponse>> triggerSonarQube(@PathVariable Long projectId) {
        PipelineResponse pipeline = pipelineService.triggerSonarQube(projectId);
        return ResponseEntity.ok(ApiResponse.success("Анализ SonarQube запущен", pipeline));
    }
}