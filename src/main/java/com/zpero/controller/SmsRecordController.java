package com.zpero.controller;

import com.zpero.common.result.PageResult;
import com.zpero.common.result.Result;
import com.zpero.dto.sms.SmsRecordQueryDTO;
import com.zpero.service.SmsRecordService;
import com.zpero.vo.sms.SmsRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sms-records")
@RequiredArgsConstructor
public class SmsRecordController {

    private final SmsRecordService smsRecordService;

    @GetMapping
    @PreAuthorize("hasRole('SCHOOL')")
    public Result<PageResult<SmsRecordVO>> queryPage(SmsRecordQueryDTO queryDTO) {
        return Result.success(smsRecordService.queryPage(queryDTO));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('SCHOOL')")
    public Result<List<SmsRecordVO>> listByStudentId(@PathVariable Long studentId) {
        return Result.success(smsRecordService.listByStudentId(studentId));
    }
}
