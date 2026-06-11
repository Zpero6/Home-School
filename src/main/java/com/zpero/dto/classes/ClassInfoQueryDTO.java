package com.zpero.dto.classes;

import lombok.Data;

@Data
public class ClassInfoQueryDTO {
    private Long page = 1L;
    private Long size = 10L;
    private String name;
    private Long collegeId;
    private Long counselorId;
    private String grade;
}
