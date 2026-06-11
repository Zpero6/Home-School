package com.zpero.vo.statistics;

import lombok.Data;

import java.util.List;

@Data
public class SendStatisticsVO {

    private String targetType;

    private SendStatisticsItemVO summary;

    private List<SendStatisticsItemVO> records;
}
