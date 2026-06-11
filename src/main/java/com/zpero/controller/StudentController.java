package com.zpero.controller;

import com.zpero.common.result.PageResult;
import com.zpero.common.result.Result;
import com.zpero.dto.student.StudentDTO;
import com.zpero.dto.student.StudentQueryDTO;
import com.zpero.entity.Student;
import com.zpero.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class StudentController {

    private  final StudentService studentService;
    @GetMapping("/students")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE','COUNSELOR')")
    public Result<PageResult<Student>> queryPage(StudentQueryDTO queryDTO){
        return Result.success(studentService.queryPage(queryDTO));
    }

    @GetMapping("/students/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE','COUNSELOR')")
    public Result<Student> getStudentById(@PathVariable Long id){
        return Result.success(studentService.getStudentById(id));
    }

    @PostMapping("/students")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE')")
    public Result<Long> createStudent(@RequestBody StudentDTO student) {
        return Result.success(studentService.createStudent(student));
    }

    @PutMapping("/students/{id}")
    public Result<Void> updateStudent(@PathVariable Long id, @RequestBody StudentDTO studentDTO) {
        studentService.updateStudent(id, studentDTO);
        return Result.success();
    }

    @DeleteMapping("/students/{id}")
    public Result<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return Result.success();
    }




}
