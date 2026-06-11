package com.zpero.dto.score;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StudentScoreDTO {

    private String courseName;

    private BigDecimal score;

    private String academicYear;

    private Integer semester;
}
