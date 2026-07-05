package com.speakmate.backend.repository;

import com.speakmate.backend.model.entity.InterviewSession;
import com.speakmate.backend.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InterviewSessionRepository extends JpaRepository<InterviewSession, UUID> {
    List<InterviewSession> findByUserOrderByStartedAtDesc(User user);
    List<InterviewSession> findByUserAndStatus(User user, String status);
    long countByUserAndStatus(User user, String status);
    Optional<InterviewSession> findFirstByUserAndStatusOrderByStartedAtDesc(User user, String status);
}
