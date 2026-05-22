package com.uees.studentservices.repository;

import com.uees.studentservices.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, UUID> {

    Optional<Certificate> findByEnrollmentId(UUID enrollmentId);

    List<Certificate> findByStudentIdOrderByIssuedAtDesc(UUID studentId);

    boolean existsByEnrollmentId(UUID enrollmentId);
}
