package com.zpero.vo.parent;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ParentLetterVO {

    private Long letterId;

    private Long studentId;

    private String studentName;

    private Long parentId;

    private String parentName;

    private Long templateId;

    private String content;

    private String status;

    private LocalDateTime sendTime;

    private LocalDateTime readTime;

    private String backgroundUrl;

    private String logoUrl;
}
