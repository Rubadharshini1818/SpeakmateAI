package com.speakmate.backend.repository;

import com.speakmate.backend.model.entity.McqSession;
import com.speakmate.backend.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface McqSessionRepository extends JpaRepository<McqSession, UUID> {
    Optional<McqSession> findByUserAndSessionDate(User user, LocalDate date);
    List<McqSession> findByUserOrderBySessionDateDesc(User user);
    List<McqSession> findByUserAndStatus(User user, String status);
    long countByUserAndStatus(User user, String status);
}
