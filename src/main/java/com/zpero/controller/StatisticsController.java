package com.zpero.controller;

import com.zpero.common.result.PageResult;
import com.zpero.common.result.Result;
import com.zpero.dto.statistics.StatisticsQueryDTO;
import com.zpero.service.StatisticsService;
import com.zpero.vo.statistics.FeedbackStatisticsVO;
import com.zpero.vo.statistics.ReadStatisticsVO;
import com.zpero.vo.statistics.SendStatisticsVO;
import com.zpero.vo.statistics.StatisticsStudentVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/send/school")
    @PreAuthorize("hasRole('SCHOOL')")
    public Result<SendStatisticsVO> getSchoolSendStatistics(StatisticsQueryDTO queryDTO) {
        return Result.success(statisticsService.getSchoolSendStatistics(queryDTO));
    }

    @GetMapping("/send/college")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE')")
    public Result<SendStatisticsVO> getCollegeSendStatistics(StatisticsQueryDTO queryDTO) {
        return Result.success(statisticsService.getCollegeSendStatistics(queryDTO));
    }

    @GetMapping("/send/class")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE','COUNSELOR')")
    public Result<SendStatisticsVO> getClassSendStatistics(StatisticsQueryDTO queryDTO) {
        return Result.success(statisticsService.getClassSendStatistics(queryDTO));
    }

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

    @GetMapping("/unsent")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE','COUNSELOR')")
    public Result<PageResult<StatisticsStudentVO>> getUnsentStudents(StatisticsQueryDTO queryDTO) {
        return Result.success(statisticsService.getUnsentStudents(queryDTO));
    }

    @GetMapping("/unread")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE','COUNSELOR')")
    public Result<PageResult<StatisticsStudentVO>> getUnreadStudents(StatisticsQueryDTO queryDTO) {
        return Result.success(statisticsService.getUnreadStudents(queryDTO));
    }

    @GetMapping("/send/export")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE')")
    public void exportSendStatistics(StatisticsQueryDTO queryDTO,
                                     HttpServletResponse response) throws IOException {
        statisticsService.exportSendStatistics(queryDTO, response);
    }

    @GetMapping("/read/export")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE')")
    public void exportReadStatistics(StatisticsQueryDTO queryDTO,
                                     HttpServletResponse response) throws IOException {
        statisticsService.exportReadStatistics(queryDTO, response);
    }

    @GetMapping("/feedback/export")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE')")
    public void exportFeedbackStatistics(StatisticsQueryDTO queryDTO,
                                         HttpServletResponse response) throws IOException {
        statisticsService.exportFeedbackStatistics(queryDTO, response);
    }
}
