package com.uees.studentservices.repository;

import com.uees.studentservices.model.EnrollmentProgress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnrollmentProgressRepository extends JpaRepository<EnrollmentProgress, UUID> {

    Optional<EnrollmentProgress> findByEnrollmentId(UUID enrollmentId);

    Optional<EnrollmentProgress> findByStudentIdAndCourseId(UUID studentId, UUID courseId);

    List<EnrollmentProgress> findByStudentId(UUID studentId);

    long countByStudentId(UUID studentId);

    long countByStudentIdAndCompletedTrue(UUID studentId);

    /**
     * Ranking de estudiantes por % de progreso en un curso (Pageable).
     */
    Page<EnrollmentProgress> findByCourseIdOrderByProgressPercentDescUpdatedAtAsc(
            UUID courseId, Pageable pageable);
}
