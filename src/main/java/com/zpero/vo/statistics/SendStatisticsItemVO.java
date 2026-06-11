package com.zpero.vo.statistics;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SendStatisticsItemVO {

    private Long targetId;

    private String targetName;

    private Long shouldSendCount;

    private Long actualSendCount;

    private Long unsentCount;

    private BigDecimal completionRate;
}
