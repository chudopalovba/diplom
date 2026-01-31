package com.devops.platform.dto.response;

import com.devops.platform.entity.PipelineStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineStageResponse {
    
    private String name;
    private String status;
    
    public static PipelineStageResponse fromEntity(PipelineStage stage) {
        return PipelineStageResponse.builder()
                .name(stage.getName())
                .status(stage.getStatus().name().toLowerCase())
                .build();
    }
}