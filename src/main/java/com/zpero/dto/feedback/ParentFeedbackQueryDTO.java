package com.zpero.dto.feedback;

import lombok.Data;

@Data
public class ParentFeedbackQueryDTO {

    private Long page = 1L;

    private Long size = 10L;

    private Long studentId;

    private Long letterId;

    private String content;
}
