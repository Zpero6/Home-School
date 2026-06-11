package com.zpero.controller;

import com.zpero.common.result.Result;
import com.zpero.dto.parent.ParentPasswordDTO;
import com.zpero.service.ParentPortalService;
import com.zpero.vo.link.ExternalLinkVO;
import com.zpero.vo.score.StudentScoreVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/parent")
@RequiredArgsConstructor
public class ParentPortalController {

    private final ParentPortalService parentPortalService;

    @GetMapping("/scores")
    @PreAuthorize("hasRole('PARENT')")
    public Result<List<StudentScoreVO>> getCurrentStudentScores() {
        return Result.success(parentPortalService.getCurrentStudentScores());
    }

    @PutMapping("/password")
    @PreAuthorize("hasRole('PARENT')")
    public Result<Void> updatePassword(@RequestBody ParentPasswordDTO dto) {
        parentPortalService.updatePassword(dto);
        return Result.success();
    }

    @GetMapping("/links")
    @PreAuthorize("hasRole('PARENT')")
    public Result<List<ExternalLinkVO>> listExternalLinks() {
        return Result.success(parentPortalService.listExternalLinks());
    }
}
