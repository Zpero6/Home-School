package com.zpero.vo.sms;

import com.zpero.entity.SmsRecord;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SmsRecordVO {

    private Long id;

    private Long studentId;

    private String studentNo;

    private String studentName;

    private Long parentId;

    private String parentName;

    private String phone;

    private String content;

    private String status;

    private String failReason;

    private LocalDateTime sendTime;

    private LocalDateTime createTime;

    public SmsRecordVO(SmsRecord record) {
        this.id = record.getId();
        this.studentId = record.getStudentId();
        this.parentId = record.getParentId();
        this.phone = record.getPhone();
        this.content = record.getContent();
        this.status = record.getStatus();
        this.failReason = record.getFailReason();
        this.sendTime = record.getSendTime();
        this.createTime = record.getCreateTime();
    }
}
