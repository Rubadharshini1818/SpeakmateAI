package com.speakmate.backend.model.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class LessonResponseDto {
    private UUID id;
    private String title;
    private String status;
    private String content;
}
