package com.zpero.controller;

import com.zpero.common.result.Result;
import com.zpero.dto.link.ExternalLinkDTO;
import com.zpero.dto.link.ExternalLinkQueryDTO;
import com.zpero.dto.link.ExternalLinkSortDTO;
import com.zpero.service.ExternalLinkService;
import com.zpero.vo.link.ExternalLinkVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/links")
@RequiredArgsConstructor
public class ExternalLinkController {

    private final ExternalLinkService externalLinkService;

    @GetMapping
    @PreAuthorize("hasRole('SCHOOL')")
    public Result<List<ExternalLinkVO>> list(ExternalLinkQueryDTO queryDTO) {
        return Result.success(externalLinkService.list(queryDTO));
    }

    @PostMapping
    @PreAuthorize("hasRole('SCHOOL')")
    public Result<Long> create(@RequestBody ExternalLinkDTO dto) {
        return Result.success(externalLinkService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SCHOOL')")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody ExternalLinkDTO dto) {
        externalLinkService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SCHOOL')")
    public Result<Void> delete(@PathVariable Long id) {
        externalLinkService.delete(id);
        return Result.success();
    }

    @PutMapping("/sort")
    @PreAuthorize("hasRole('SCHOOL')")
    public Result<Void> updateSort(@RequestBody ExternalLinkSortDTO dto) {
        externalLinkService.updateSort(dto);
        return Result.success();
    }
}
