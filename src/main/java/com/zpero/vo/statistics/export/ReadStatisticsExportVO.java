package com.zpero.vo.statistics.export;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ColumnWidth(18)
public class ReadStatisticsExportVO {

    @ExcelProperty("应发送人数")
    private Long shouldSendCount;

    @ExcelProperty("实际发送人数")
    private Long actualSendCount;

    @ExcelProperty("已查阅人数")
    private Long readCount;

    @ExcelProperty("未查阅人数")
    private Long unreadCount;

    @ExcelProperty("查阅率")
    private BigDecimal readRate;
}
