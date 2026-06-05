package com.zpero.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zpero.entity.baseEntity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("letter_template")
@EqualsAndHashCode(callSuper = true)
public class LetterTemplate extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String content;

    private String backgroundUrl;

    private String logoUrl;

    private Long creatorId;

    private String creatorType;

    private Long collegeId;

    private Integer isShared;

    private Long sourceTemplateId;
}
