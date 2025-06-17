package com.collabrix.collabrix.repository;

import com.collabrix.collabrix.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findByOwnerId(Long ownerId);

    boolean existsByNameAndOwnerId(String name, Long ownerId);
}
