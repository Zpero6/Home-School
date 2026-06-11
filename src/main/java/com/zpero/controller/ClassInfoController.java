package com.zpero.controller;

import com.zpero.common.result.PageResult;
import com.zpero.common.result.Result;
import com.zpero.dto.classes.ClassInfoDTO;
import com.zpero.dto.classes.ClassInfoQueryDTO;
import com.zpero.entity.ClassInfo;
import com.zpero.service.ClassInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ClassInfoController {

    private final ClassInfoService classInfoService;
    @GetMapping("/classes")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE','COUNSELOR')")
    public Result<PageResult<ClassInfo>> queryPage(ClassInfoQueryDTO queryDTO) {
        return Result.success(classInfoService.queryPage(queryDTO));
    }

    @GetMapping("/classes/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE','COUNSELOR')")
    public Result<ClassInfo> getById(@PathVariable Long id) {
        return Result.success(classInfoService.getById(id));
    }

    @PostMapping("/classes")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE')")
    public Result<Long> createClassInfo(@RequestBody ClassInfoDTO dto) {
        return Result.success(classInfoService.createClassInfo(dto));
    }
    @PutMapping("/classes/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE')")
    public Result<Void> updateClassInfo(@PathVariable Long id, @RequestBody ClassInfoDTO dto) {
        classInfoService.updateClassInfo(id, dto);
        return Result.success();
    }
    @DeleteMapping("/classes/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE')")
    public Result<Void> deleteClassInfo(@PathVariable Long id) {
        classInfoService.deleteClassInfo(id);
        return Result.success();
    }
}
