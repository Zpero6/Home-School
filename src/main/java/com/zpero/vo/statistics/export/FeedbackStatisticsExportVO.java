package com.zpero.vo.statistics.export;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ColumnWidth(18)
public class FeedbackStatisticsExportVO {

    @ExcelProperty("应发送人数")
    private Long shouldSendCount;

    @ExcelProperty("实际发送人数")
    private Long actualSendCount;

    @ExcelProperty("反馈学生数")
    private Long feedbackStudentCount;

    @ExcelProperty("未反馈人数")
    private Long noFeedbackCount;

    @ExcelProperty("反馈总数")
    private Long totalFeedbackCount;

    @ExcelProperty("反馈率")
    private BigDecimal feedbackRate;
}
