package com.speakmate.backend.repository;

import com.speakmate.backend.model.entity.User;
import com.speakmate.backend.model.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, UUID> {
    List<UserBadge> findByUserOrderByEarnedAtDesc(User user);
    boolean existsByUserAndBadge_Code(User user, String badgeCode);
    long countByUser(User user);
}
