package com.devops.platform.dto.response;

import com.devops.platform.entity.Pipeline;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
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
        return PipelineResponse.builder()
                .id(pipeline.getId())
                .status(pipeline.getStatus().name().toLowerCase())
                .stages(pipeline.getStages().stream()
                        .map(PipelineStageResponse::fromEntity)
                        .collect(Collectors.toList()))
                .deployUrl(pipeline.getDeployUrl())
                .startedAt(pipeline.getStartedAt())
                .finishedAt(pipeline.getFinishedAt())
                .build();
    }
}