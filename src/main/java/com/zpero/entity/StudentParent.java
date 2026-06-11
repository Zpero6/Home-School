package com.zpero.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zpero.entity.baseEntity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("student_parent")
@EqualsAndHashCode(callSuper = true)
public class StudentParent extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long studentId;

    private String name;

    private String relation;

    private String phone;

    private Integer isDefault;

    private String sourceType;
}
