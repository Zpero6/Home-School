package com.zpero.vo.feedback;

import com.zpero.entity.ParentFeedback;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ParentFeedbackVO {

    private Long id;

    private Long letterId;

    private Long studentId;

    private String studentName;

    private Long parentId;

    private String parentName;

    private Long collegeId;

    private Long counselorId;

    private String content;

    private String images;

    private LocalDateTime createTime;

    public ParentFeedbackVO(ParentFeedback feedback) {
        this.id = feedback.getId();
        this.letterId = feedback.getLetterId();
        this.studentId = feedback.getStudentId();
        this.parentId = feedback.getParentId();
        this.content = feedback.getContent();
        this.images = feedback.getImages();
        this.createTime = feedback.getCreateTime();
    }
}
