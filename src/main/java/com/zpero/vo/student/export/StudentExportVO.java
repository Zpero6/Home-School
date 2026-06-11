package com.zpero.vo.student.export;

import com.alibaba.excel.annotation.ExcelProperty;
import com.zpero.entity.Student;
import lombok.Data;

@Data
public class StudentExportVO {

    @ExcelProperty("学号")
    private String studentNo;

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("身份证号")
    private String idCard;

    @ExcelProperty("学院ID")
    private Long collegeId;

    @ExcelProperty("班级ID")
    private Long classId;

    @ExcelProperty("辅导员ID")
    private Long counselorId;

    @ExcelProperty("入学年份")
    private String enrollmentYear;

    @ExcelProperty("状态")
    private String status;

    public StudentExportVO(Student student) {
        this.studentNo = student.getStudentNo();
        this.name = student.getName();
        this.idCard = student.getIdCard();
        this.collegeId = student.getCollegeId();
        this.classId = student.getClassId();
        this.counselorId = student.getCounselorId();
        this.enrollmentYear = student.getEnrollmentYear();
        this.status = student.getStatus();
    }
}
