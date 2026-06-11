package com.zpero.controller;

import com.zpero.common.result.Result;
import com.zpero.entity.ClassInfo;
import com.zpero.entity.College;
import com.zpero.service.BasicDataService;
import com.zpero.vo.Counselor.CounselorVo;
import com.zpero.vo.student.StatusVo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/basic")
@RequiredArgsConstructor
public class BasicDataController {

    private final BasicDataService basicDataService;

    @GetMapping("/college")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE','COUNSELOR','PARENT')")
    public Result<List<College>> listCollege() {
        return Result.success(basicDataService.listCollege());
    }

    @GetMapping("/classes")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE','COUNSELOR')")
    public Result<List<ClassInfo>> listClassInfo(@RequestParam(required = false) Long collegeId) {
        return Result.success(basicDataService.listClassInfo(collegeId));
    }

    @GetMapping("/counselors")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE','COUNSELOR')")
    public Result<List<CounselorVo>> listCounselors(@RequestParam(required = false) Long collegeId) {
        return Result.success(basicDataService.listCounselors(collegeId));
    }

    @GetMapping("/student-statuses")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE','COUNSELOR','PARENT')")
    public Result<List<StatusVo>> listStudentStatuses() {
        return Result.success(basicDataService.listStudentStatuses());
    }

}
