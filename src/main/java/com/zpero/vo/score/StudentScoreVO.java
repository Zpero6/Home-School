package com.zpero.vo.score;

import com.zpero.entity.StudentScore;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StudentScoreVO {

    private Long id;

    private Long studentId;

    private String courseName;

    private BigDecimal score;

    private String academicYear;

    private Integer semester;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public StudentScoreVO(StudentScore score) {
        this.id = score.getId();
        this.studentId = score.getStudentId();
        this.courseName = score.getCourseName();
        this.score = score.getScore();
        this.academicYear = score.getAcademicYear();
        this.semester = score.getSemester();
        this.createTime = score.getCreateTime();
        this.updateTime = score.getUpdateTime();
    }
}
