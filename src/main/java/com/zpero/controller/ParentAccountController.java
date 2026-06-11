package com.zpero.controller;

import com.zpero.common.result.PageResult;
import com.zpero.common.result.Result;
import com.zpero.dto.parent.ParentAccountDTO;
import com.zpero.dto.parent.ParentAccountQueryDTO;
import com.zpero.service.ParentAccountService;
import com.zpero.vo.parent.ParentAccountVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/parent-accounts")
@RequiredArgsConstructor
public class ParentAccountController {

    private final ParentAccountService parentAccountService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE','COUNSELOR')")
    public Result<PageResult<ParentAccountVO>> queryPage(ParentAccountQueryDTO queryDTO) {
        return Result.success(parentAccountService.queryPage(queryDTO));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE','COUNSELOR')")
    public Result<ParentAccountVO> getById(@PathVariable Long id) {
        return Result.success(parentAccountService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE','COUNSELOR')")
    public Result<Long> createParentAccount(@RequestBody ParentAccountDTO dto) {
        return Result.success(parentAccountService.createParentAccount(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE','COUNSELOR')")
    public Result<Void> updateParentAccount(@PathVariable Long id,
                                            @RequestBody ParentAccountDTO dto) {
        parentAccountService.updateParentAccount(id, dto);
        return Result.success();
    }

    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE')")
    @DeleteMapping("/{id}")
    public Result<Void> deleteParentAccount(@PathVariable Long id) {
        parentAccountService.deleteParentAccount(id);
        return Result.success();
    }
}
