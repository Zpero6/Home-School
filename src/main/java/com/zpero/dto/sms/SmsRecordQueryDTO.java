package com.zpero.dto.sms;

import lombok.Data;

@Data
public class SmsRecordQueryDTO {

    private Long page = 1L;

    private Long size = 10L;

    private Long studentId;

    private Long parentId;

    private String phone;

    private String status;
}
