package com.zpero.dto.statistics;

import lombok.Data;

@Data
public class StatisticsQueryDTO {

    private Long page = 1L;

    private Long size = 10L;

    private Long collegeId;

    private Long counselorId;

    private Long classId;
}
