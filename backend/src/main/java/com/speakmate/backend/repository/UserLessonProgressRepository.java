package com.speakmate.backend.repository;

import com.speakmate.backend.model.entity.Lesson;
import com.speakmate.backend.model.entity.User;
import com.speakmate.backend.model.entity.UserLessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserLessonProgressRepository extends JpaRepository<UserLessonProgress, UUID> {
    List<UserLessonProgress> findByUserAndLesson_Module(User user, String module);
    Optional<UserLessonProgress> findByUserAndLesson(User user, Lesson lesson);
    List<UserLessonProgress> findByUser(User user);
}
