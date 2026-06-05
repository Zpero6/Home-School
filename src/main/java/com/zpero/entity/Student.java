package com.zpero.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zpero.entity.baseEntity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("student")
@EqualsAndHashCode(callSuper = true)
public class Student extends BaseEntity {

    // 学院id, 班级id, 学号 ,身份证号,姓名, 年级, 入学年份, 班级id, 状态
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String studentNo;

    private String name;

    private String idCard;

    private Long collegeId;

    private Long classId;
    // 辅导员id
    private Long counselorId;
    // 入学年份
    private String enrollmentYear;

    private String status;
}
