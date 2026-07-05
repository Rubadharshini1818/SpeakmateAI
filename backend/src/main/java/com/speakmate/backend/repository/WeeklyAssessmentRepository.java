package com.speakmate.backend.repository;

import com.speakmate.backend.model.entity.User;
import com.speakmate.backend.model.entity.WeeklyAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WeeklyAssessmentRepository extends JpaRepository<WeeklyAssessment, UUID> {
    Optional<WeeklyAssessment> findByUserAndWeekStart(User user, LocalDate weekStart);
    List<WeeklyAssessment> findByUserOrderByWeekStartDesc(User user);
    Optional<WeeklyAssessment> findFirstByUserAndStatusOrderByWeekStartDesc(User user, String status);
}
