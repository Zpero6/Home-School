package com.zpero.service;

import com.zpero.common.result.PageResult;
import com.zpero.dto.statistics.StatisticsQueryDTO;
import com.zpero.vo.statistics.FeedbackStatisticsVO;
import com.zpero.vo.statistics.ReadStatisticsVO;
import com.zpero.vo.statistics.SendStatisticsVO;
import com.zpero.vo.statistics.StatisticsStudentVO;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface StatisticsService {

    SendStatisticsVO getSchoolSendStatistics(StatisticsQueryDTO queryDTO);

    SendStatisticsVO getCollegeSendStatistics(StatisticsQueryDTO queryDTO);

    SendStatisticsVO getClassSendStatistics(StatisticsQueryDTO queryDTO);

    ReadStatisticsVO getReadStatistics(StatisticsQueryDTO queryDTO);

    FeedbackStatisticsVO getFeedbackStatistics(StatisticsQueryDTO queryDTO);

    PageResult<StatisticsStudentVO> getUnsentStudents(StatisticsQueryDTO queryDTO);

    PageResult<StatisticsStudentVO> getUnreadStudents(StatisticsQueryDTO queryDTO);

    void exportSendStatistics(StatisticsQueryDTO queryDTO, HttpServletResponse response) throws IOException;

    void exportReadStatistics(StatisticsQueryDTO queryDTO, HttpServletResponse response) throws IOException;

    void exportFeedbackStatistics(StatisticsQueryDTO queryDTO, HttpServletResponse response) throws IOException;
}
