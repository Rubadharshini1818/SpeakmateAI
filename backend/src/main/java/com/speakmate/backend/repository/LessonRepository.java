package com.speakmate.backend.repository;

import com.speakmate.backend.model.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    List<Lesson> findByModuleOrderByOrderIndexAsc(String module);
    java.util.Optional<Lesson> findByModuleAndTitle(String module, String title);
}

