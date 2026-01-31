package com.devops.platform.repository;

import com.devops.platform.entity.Project;
import com.devops.platform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    List<Project> findByOwnerOrderByCreatedAtDesc(User owner);
    
    Optional<Project> findByIdAndOwner(Long id, User owner);
    
    boolean existsByNameAndOwner(String name, User owner);
    
    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.pipelines WHERE p.id = :id AND p.owner = :owner")
    Optional<Project> findByIdAndOwnerWithPipelines(@Param("id") Long id, @Param("owner") User owner);
    
    @Query("SELECT COUNT(p) FROM Project p WHERE p.owner = :owner")
    long countByOwner(@Param("owner") User owner);
    
    @Query("SELECT COUNT(p) FROM Project p WHERE p.owner = :owner AND p.status = 'DEPLOYED'")
    long countDeployedByOwner(@Param("owner") User owner);
}