package com.zpero.controller;

import com.zpero.common.result.Result;
import com.zpero.dto.parent.ParentFeedbackDTO;
import com.zpero.service.ParentLetterService;
import com.zpero.vo.parent.ParentLetterVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/parent")
@RequiredArgsConstructor
public class ParentLetterController {

    private final ParentLetterService parentLetterService;

    @GetMapping("/letter")
    @PreAuthorize("hasRole('PARENT')")
    public Result<ParentLetterVO> getCurrentParentLetter() {
        return Result.success(parentLetterService.getCurrentParentLetter());
    }

    @PostMapping("/feedback")
    @PreAuthorize("hasRole('PARENT')")
    public Result<Long> submitFeedback(@RequestBody ParentFeedbackDTO dto) {
        return Result.success(parentLetterService.submitFeedback(dto));
    }
}
