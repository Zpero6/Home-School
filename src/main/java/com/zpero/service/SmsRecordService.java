package com.zpero.service;

import com.zpero.common.result.PageResult;
import com.zpero.dto.sms.SmsRecordQueryDTO;
import com.zpero.vo.sms.SmsRecordVO;

import java.util.List;

public interface SmsRecordService {

    PageResult<SmsRecordVO> queryPage(SmsRecordQueryDTO queryDTO);

    List<SmsRecordVO> listByStudentId(Long studentId);
}
