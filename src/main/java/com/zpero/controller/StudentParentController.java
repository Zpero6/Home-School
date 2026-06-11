package com.zpero.controller;

import com.zpero.common.result.Result;
import com.zpero.dto.parent.StudentParentDTO;
import com.zpero.entity.StudentParent;
import com.zpero.service.StudentParentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StudentParentController {

    private final StudentParentService studentParentService;

    @GetMapping("/api/v1/students/{studentId}/parents")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE','COUNSELOR')")
    public Result<List<StudentParent>> listByStudentId(@PathVariable Long studentId) {
        return Result.success(studentParentService.listByStudentId(studentId));
    }

    @PostMapping("/api/v1/students/{studentId}/parents")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE','COUNSELOR')")
    public Result<Long> createStudentParent(@PathVariable Long studentId,
                                            @RequestBody StudentParentDTO dto) {
        return Result.success(studentParentService.createStudentParent(studentId, dto));
    }

    @PutMapping("/api/v1/student-parents/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE','COUNSELOR')")
    public Result<Void> updateStudentParent(@PathVariable Long id,
                                            @RequestBody StudentParentDTO dto) {
        studentParentService.updateStudentParent(id, dto);
        return Result.success();
    }

    @DeleteMapping("/api/v1/student-parents/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE')")
    public Result<Void> deleteStudentParent(@PathVariable Long id) {
        studentParentService.deleteStudentParent(id);
        return Result.success();
    }

    @PutMapping("/api/v1/student-parents/{id}/default")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE','COUNSELOR')")
    public Result<Void> setDefaultParent(@PathVariable Long id) {
        studentParentService.setDefaultParent(id);
        return Result.success();
    }
}
