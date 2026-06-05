package com.zpero.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sms_record")
public class SmsRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    private Long parentId;

    private String phone;

    private String content;

    private String status;

    private String failReason;

    private LocalDateTime sendTime;

    private LocalDateTime createTime;
}
