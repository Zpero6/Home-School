package com.zpero.controller;

import com.zpero.common.result.Result;
import com.zpero.dto.statistics.StatisticsQueryDTO;
import com.zpero.service.StatisticsService;
import com.zpero.vo.statistics.FeedbackStatisticsVO;
import com.zpero.vo.statistics.ReadStatisticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/read")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE','COUNSELOR')")
    public Result<ReadStatisticsVO> getReadStatistics(StatisticsQueryDTO queryDTO) {
        return Result.success(statisticsService.getReadStatistics(queryDTO));
    }

    @GetMapping("/feedback")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE','COUNSELOR')")
    public Result<FeedbackStatisticsVO> getFeedbackStatistics(StatisticsQueryDTO queryDTO) {
        return Result.success(statisticsService.getFeedbackStatistics(queryDTO));
    }
}
