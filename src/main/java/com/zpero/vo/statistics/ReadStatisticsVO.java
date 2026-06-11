package com.zpero.vo.statistics;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReadStatisticsVO {

    private Long shouldSendCount;

    private Long actualSendCount;

    private Long readCount;

    private Long unreadCount;

    private BigDecimal readRate;
}
