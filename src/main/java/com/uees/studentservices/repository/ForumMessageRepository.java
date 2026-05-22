package com.uees.studentservices.repository;

import com.uees.studentservices.model.ForumMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ForumMessageRepository extends JpaRepository<ForumMessage, UUID> {

    Page<ForumMessage> findByCourseIdOrderByCreatedAtDesc(UUID courseId, Pageable pageable);
}
