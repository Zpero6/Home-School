package com.zpero.controller;

import com.zpero.common.result.PageResult;
import com.zpero.common.result.Result;
import com.zpero.dto.feedback.ParentFeedbackQueryDTO;
import com.zpero.service.ParentFeedbackService;
import com.zpero.vo.feedback.ParentFeedbackVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/feedbacks")
@RequiredArgsConstructor
public class ParentFeedbackController {

    private final ParentFeedbackService parentFeedbackService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE','COUNSELOR')")
    public Result<PageResult<ParentFeedbackVO>> queryPage(ParentFeedbackQueryDTO queryDTO) {
        return Result.success(parentFeedbackService.queryPage(queryDTO));
    }
}
