package com.uees.studentservices.service;

import com.uees.studentservices.dto.*;
import com.uees.studentservices.exception.DomainException;
import com.uees.studentservices.model.EnrollmentProgress;
import com.uees.studentservices.model.Student;
import com.uees.studentservices.repository.CertificateRepository;
import com.uees.studentservices.repository.EnrollmentProgressRepository;
import com.uees.studentservices.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final StudentRepository students;
    private final EnrollmentProgressRepository progressRepo;
    private final CertificateRepository certificates;
    private final LearningEngineClient learningEngine;

    public DashboardService(StudentRepository students,
                            EnrollmentProgressRepository progressRepo,
                            CertificateRepository certificates,
                            LearningEngineClient learningEngine) {
        this.students = students;
        this.progressRepo = progressRepo;
        this.certificates = certificates;
        this.learningEngine = learningEngine;
    }

    @Transactional(readOnly = true)
    public DashboardResponse buildFor(UUID studentId) {
        Student student = students.findById(studentId)
                .orElseThrow(() -> DomainException.notFound("Estudiante no encontrado"));

        List<EnrollmentProgress> enrollments = progressRepo.findByStudentId(studentId);

        // Catalogo desde Grupo A (WebClient)
        List<CourseDto> catalog = learningEngine.fetchCourses(0, 12, null);
        Map<UUID, CourseDto> catalogById = catalog.stream()
                .filter(c -> c.id() != null)
                .collect(Collectors.toMap(CourseDto::id, c -> c, (a, b) -> a));

        List<EnrolledCourseDto> enrolledCourses = enrollments.stream()
                .map(ep -> {
                    CourseDto match = catalogById.get(ep.getCourseId());
                    return new EnrolledCourseDto(
                            ep.getEnrollmentId(),
                            ep.getCourseId(),
                            ep.getCourseName(),
                            ep.getProgressPercent(),
                            ep.getModulesCompleted(),
                            ep.isCompleted(),
                            match != null ? match.imageUrl() : null,
                            match != null ? match.instructor() : null
                    );
                })
                .toList();

        long completed = enrollments.stream().filter(EnrollmentProgress::isCompleted).count();
        int overall = enrollments.isEmpty()
                ? 0
                : (int) Math.round(enrollments.stream()
                        .mapToInt(EnrollmentProgress::getProgressPercent)
                        .average().orElse(0));

        List<CertificateDto> certDtos = certificates.findByStudentIdOrderByIssuedAtDesc(studentId)
                .stream()
                .map(CertificateDto::fromEntity)
                .toList();

        return new DashboardResponse(
                student.getId(),
                student.getEmail(),
                student.getFullName(),
                student.getProfilePictureUrl(),
                enrollments.size(),
                completed,
                overall,
                enrolledCourses,
                certDtos,
                catalog
        );
    }
}
