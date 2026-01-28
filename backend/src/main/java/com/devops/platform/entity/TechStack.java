package com.devops.platform.entity;

import com.devops.entity.enums.BackendTech;
import com.devops.entity.enums.DatabaseTech;
import com.devops.entity.enums.FrontendTech;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class TechStack{

    @Enumerated(EnumType.STRING)
    @Column(name = "backend_tech")
    private BackendTech backend;

    @Enumerated(EnumType.STRING)
    @Column(name = "frontend_tech")
    private FrontendTech frontend;

    @Enumerated(EnumType.STRING)
    @Column(name = "database_tech")
    private DatabaseTech database;

    @Column(name = "use_docker")
    private Boolean useDocker;
}