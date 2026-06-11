package com.zpero.vo.statistics;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FeedbackStatisticsVO {

    private Long shouldSendCount;

    private Long actualSendCount;

    private Long feedbackStudentCount;

    private Long noFeedbackCount;

    private Long totalFeedbackCount;

    private BigDecimal feedbackRate;
}
