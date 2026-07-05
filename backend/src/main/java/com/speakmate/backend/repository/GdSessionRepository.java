package com.speakmate.backend.repository;

import com.speakmate.backend.model.entity.GdSession;
import com.speakmate.backend.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GdSessionRepository extends JpaRepository<GdSession, UUID> {
    List<GdSession> findByUserOrderByStartedAtDesc(User user);
    long countByUserAndStatus(User user, String status);
    Optional<GdSession> findFirstByUserAndStatusOrderByStartedAtDesc(User user, String status);
}
