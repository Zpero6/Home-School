package com.zpero.controller;

import com.zpero.common.result.PageResult;
import com.zpero.common.result.Result;
import com.zpero.dto.college.CollegeDTO;
import com.zpero.dto.college.CollegeQueryDTO;
import com.zpero.entity.College;
import com.zpero.service.CollegeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CollegeController {

    private final CollegeService collegeService;

    @GetMapping("/college")
    @PreAuthorize("hasRole('ROLE_SCHOOL')")
    public Result<PageResult<College>> listCollege(CollegeQueryDTO collegeDTO) {
        return Result.success(collegeService.queryPage(collegeDTO));
    }

    @PostMapping("/college")
    @PreAuthorize("hasRole('ROLE_SCHOOL')")
    public Result<Long> createCollege(CollegeDTO collegeDTO) {
        return Result.success(collegeService.createCollege(collegeDTO));
    }

    @PostMapping("/college/{id}")
    @PreAuthorize("hasRole('ROLE_SCHOOL')")
    public Result updateCollege(@PathVariable Long id, @RequestBody CollegeDTO collegeDTO) {
        collegeService.updateCollege(id, collegeDTO);
        return Result.success();
    }

    @DeleteMapping("/college/{id}")
    @PreAuthorize("hasRole('ROLE_SCHOOL')")
    public Result deleteCollege(@PathVariable Long id) {
        collegeService.deleteCollege(id);
        return Result.success();
    }

}
