package com.zpero.service;

import com.zpero.common.result.PageResult;
import com.zpero.dto.feedback.ParentFeedbackQueryDTO;
import com.zpero.vo.feedback.ParentFeedbackVO;

public interface ParentFeedbackService {

    PageResult<ParentFeedbackVO> queryPage(ParentFeedbackQueryDTO queryDTO);
}
