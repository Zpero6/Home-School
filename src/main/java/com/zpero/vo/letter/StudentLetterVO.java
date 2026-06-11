package com.zpero.vo.letter;

import com.zpero.entity.StudentLetter;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudentLetterVO {

    private Long id;

    private Long studentId;

    private String studentName;

    private Long parentId;

    private String parentName;

    private Long templateId;

    private String content;

    private String status;

    private LocalDateTime readTime;

    private LocalDateTime sendTime;

    private Long counselorId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public StudentLetterVO(StudentLetter letter) {
        this.id = letter.getId();
        this.studentId = letter.getStudentId();
        this.parentId = letter.getParentId();
        this.templateId = letter.getTemplateId();
        this.content = letter.getContent();
        this.status = letter.getStatus();
        this.readTime = letter.getReadTime();
        this.sendTime = letter.getSendTime();
        this.counselorId = letter.getCounselorId();
        this.createTime = letter.getCreateTime();
        this.updateTime = letter.getUpdateTime();
    }
}
