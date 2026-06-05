package com.zpero.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zpero.entity.baseEntity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("student_letter")
@EqualsAndHashCode(callSuper = true)
public class StudentLetter extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    private Long parentId;

    private Long templateId;

    private String content;

    private String status;

    private LocalDateTime readTime;

    private LocalDateTime sendTime;

    private Long counselorId;
}
