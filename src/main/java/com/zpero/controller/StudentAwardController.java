package com.zpero.controller;

import com.zpero.common.result.Result;
import com.zpero.dto.award.StudentAwardDTO;
import com.zpero.service.StudentAwardService;
import com.zpero.vo.award.StudentAwardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StudentAwardController {

    private final StudentAwardService studentAwardService;

    @GetMapping("/api/v1/students/{studentId}/awards")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE','COUNSELOR')")
    public Result<List<StudentAwardVO>> listByStudentId(@PathVariable Long studentId) {
        return Result.success(studentAwardService.listByStudentId(studentId));
    }

    @PostMapping("/api/v1/students/{studentId}/awards")
    @PreAuthorize("hasRole('COUNSELOR')")
    public Result<Long> createAward(@PathVariable Long studentId,
                                    @RequestBody StudentAwardDTO dto) {
        return Result.success(studentAwardService.createAward(studentId, dto));
    }

    @PutMapping("/api/v1/awards/{id}")
    @PreAuthorize("hasRole('COUNSELOR')")
    public Result<Void> updateAward(@PathVariable Long id,
                                    @RequestBody StudentAwardDTO dto) {
        studentAwardService.updateAward(id, dto);
        return Result.success();
    }

    @DeleteMapping("/api/v1/awards/{id}")
    @PreAuthorize("hasRole('COUNSELOR')")
    public Result<Void> deleteAward(@PathVariable Long id) {
        studentAwardService.deleteAward(id);
        return Result.success();
    }
}
