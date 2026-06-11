package com.zpero.service;


import com.zpero.common.result.PageResult;
import com.zpero.dto.student.StudentDTO;
import com.zpero.dto.student.StudentQueryDTO;
import com.zpero.entity.Student;
import jakarta.servlet.http.HttpServletResponse;

public interface StudentService {

    PageResult<Student> queryPage(StudentQueryDTO queryDTO);

    Student getStudentById(Long id);

    Long createStudent(StudentDTO studentDTO);

    void updateStudent(Long id, StudentDTO studentDTO);

    void deleteStudent(Long id);

    void exportStudents(StudentQueryDTO queryDTO, HttpServletResponse response);
}
