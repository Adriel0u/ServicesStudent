package com.uees.studentservices.repository;

import com.uees.studentservices.model.ModuleCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ModuleCompletionRepository extends JpaRepository<ModuleCompletion, UUID> {

    boolean existsByEnrollmentIdAndModuleId(UUID enrollmentId, UUID moduleId);

    long countByEnrollmentId(UUID enrollmentId);
}
