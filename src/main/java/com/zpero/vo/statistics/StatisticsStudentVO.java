package com.zpero.vo.statistics;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StatisticsStudentVO {

    private Long studentId;

    private String studentNo;

    private String studentName;

    private String studentStatus;

    private Long collegeId;

    private String collegeName;

    private Long classId;

    private String className;

    private Long counselorId;

    private String counselorName;

    private Long letterId;

    private String sendStatus;

    private String readStatus;

    private LocalDateTime sendTime;

    private LocalDateTime readTime;
}
