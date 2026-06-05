package com.zpero.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zpero.entity.baseEntity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("student_award")
@EqualsAndHashCode(callSuper = true)
public class StudentAward extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    private String awardName;

    private String awardLevel;

    private LocalDateTime awardTime;
}
