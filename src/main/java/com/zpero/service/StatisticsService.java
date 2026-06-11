package com.zpero.service;

import com.zpero.dto.statistics.StatisticsQueryDTO;
import com.zpero.vo.statistics.FeedbackStatisticsVO;
import com.zpero.vo.statistics.ReadStatisticsVO;

public interface StatisticsService {

    ReadStatisticsVO getReadStatistics(StatisticsQueryDTO queryDTO);

    FeedbackStatisticsVO getFeedbackStatistics(StatisticsQueryDTO queryDTO);
}
