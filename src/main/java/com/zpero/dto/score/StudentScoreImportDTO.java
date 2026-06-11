package com.zpero.dto.score;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StudentScoreImportDTO {

    @ExcelProperty("学号")
    private String studentNo;

    @ExcelProperty("课程名称")
    private String courseName;

    @ExcelProperty("成绩")
    private BigDecimal score;

    @ExcelProperty("学年")
    private String academicYear;

    @ExcelProperty("学期")
    private Integer semester;
}
