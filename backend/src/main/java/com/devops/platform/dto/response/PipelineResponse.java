package com.devops.platform.dto.response;

import com.devops.platform.entity.Pipeline;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PipelineResponse {
    
    private Long id;
    private String status;
    private List<PipelineStageResponse> stages;
    private String deployUrl;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    
    public static PipelineResponse fromEntity(Pipeline pipeline) {
        PipelineResponse response = new PipelineResponse();
        response.setId(pipeline.getId());
        response.setStatus(pipeline.getStatus().name().toLowerCase());
        response.setStages(pipeline.getStages().stream()
                .map(PipelineStageResponse::fromEntity)
                .collect(Collectors.toList()));
        response.setDeployUrl(pipeline.getDeployUrl());
        response.setStartedAt(pipeline.getStartedAt());
        response.setFinishedAt(pipeline.getFinishedAt());
        return response;
    }
}