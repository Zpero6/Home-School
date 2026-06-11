package com.zpero.dto.student;

import lombok.Data;

@Data
public class StudentQueryDTO {
    private  Long page = 1L;
    private Long size = 10L;
    private  String name ;
    private String studentNo ;
    private Long collegeId ;
    private Long classId;
    private String status;
}
