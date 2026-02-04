package com.devops.platform.dto.response;

import com.devops.platform.entity.PipelineStage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PipelineStageResponse {
    
    private String name;
    private String status;
    
    public static PipelineStageResponse fromEntity(PipelineStage stage) {
        PipelineStageResponse response = new PipelineStageResponse();
        response.setName(stage.getName());
        response.setStatus(stage.getStatus().name().toLowerCase());
        return response;
    }
}