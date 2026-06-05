package com.zpero.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zpero.entity.baseEntity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@TableName("student_score")
@EqualsAndHashCode(callSuper = true)
public class StudentScore extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    private String courseName;

    private BigDecimal score;

    private String academicYear;

    private Integer semester;
}
