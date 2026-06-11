package com.zpero.controller;

import com.zpero.common.result.PageResult;
import com.zpero.common.result.Result;
import com.zpero.dto.template.TemplateDTO;
import com.zpero.dto.template.TemplateQueryDTO;
import com.zpero.dto.template.TemplateShareDTO;
import com.zpero.service.LetterTemplateService;
import com.zpero.vo.template.LetterTemplateVO;
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

@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
public class LetterTemplateController {

    private final LetterTemplateService letterTemplateService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE','COUNSELOR')")
    public Result<PageResult<LetterTemplateVO>> listMine(TemplateQueryDTO queryDTO) {
        return Result.success(letterTemplateService.listMine(queryDTO));
    }

    @GetMapping("/shared")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE','COUNSELOR')")
    public Result<PageResult<LetterTemplateVO>> listShared(TemplateQueryDTO queryDTO) {
        return Result.success(letterTemplateService.listShared(queryDTO));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE','COUNSELOR')")
    public Result<LetterTemplateVO> getById(@PathVariable Long id) {
        return Result.success(letterTemplateService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE','COUNSELOR')")
    public Result<Long> createTemplate(@RequestBody TemplateDTO dto) {
        return Result.success(letterTemplateService.createTemplate(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE','COUNSELOR')")
    public Result<Void> updateTemplate(@PathVariable Long id,
                                       @RequestBody TemplateDTO dto) {
        letterTemplateService.updateTemplate(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE','COUNSELOR')")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        letterTemplateService.deleteTemplate(id);
        return Result.success();
    }

    @PutMapping("/{id}/share")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE','COUNSELOR')")
    public Result<Void> updateShareStatus(@PathVariable Long id,
                                          @RequestBody TemplateShareDTO dto) {
        letterTemplateService.updateShareStatus(id, dto);
        return Result.success();
    }

    @PostMapping("/{id}/copy")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE','COUNSELOR')")
    public Result<Long> copyTemplate(@PathVariable Long id) {
        return Result.success(letterTemplateService.copyTemplate(id));
    }
}
