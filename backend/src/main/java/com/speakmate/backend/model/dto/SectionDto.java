package com.speakmate.backend.model.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class SectionDto {
    private String section;
    private List<LessonResponseDto> lessons;
}
