package com.zpero.vo.statistics.export;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ColumnWidth(18)
public class SendStatisticsExportVO {

    @ExcelProperty("统计维度")
    private String targetTypeName;

    @ExcelProperty("维度ID")
    private Long targetId;

    @ExcelProperty("维度名称")
    private String targetName;

    @ExcelProperty("应发送人数")
    private Long shouldSendCount;

    @ExcelProperty("实际发送人数")
    private Long actualSendCount;

    @ExcelProperty("未发送人数")
    private Long unsentCount;

    @ExcelProperty("完成率")
    private BigDecimal completionRate;
}
