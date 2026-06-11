package com.zpero.dto.student;

import lombok.Data;

@Data
public class StudentDTO {
    private String studentNo;
    private String name;
    private String idCard;
    private Long collegeId;
    private Long classId;
    private Long counselorId;
    private String enrollmentYear;
    private String status;
}
