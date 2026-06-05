package com.zpero.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("parent_feedback")
public class ParentFeedback {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long letterId;

    private Long studentId;

    private Long parentId;

    private String content;

    private String images;

    private LocalDateTime createTime;
}
