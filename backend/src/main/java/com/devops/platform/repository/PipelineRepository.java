package com.devops.platform.repository;

import com.devops.platform.entity.Pipeline;
import com.devops.platform.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PipelineRepository extends JpaRepository<Pipeline, Long> {
    
    List<Pipeline> findByProjectOrderByStartedAtDesc(Project project);
    
    @Query("SELECT p FROM Pipeline p LEFT JOIN FETCH p.stages WHERE p.project = :project ORDER BY p.startedAt DESC")
    List<Pipeline> findByProjectWithStages(@Param("project") Project project);
    
    Optional<Pipeline> findTopByProjectOrderByStartedAtDesc(Project project);
    
    @Query("SELECT p FROM Pipeline p LEFT JOIN FETCH p.stages WHERE p.id = :id")
    Optional<Pipeline> findByIdWithStages(@Param("id") Long id);
}