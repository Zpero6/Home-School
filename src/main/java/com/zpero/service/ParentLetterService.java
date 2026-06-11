package com.zpero.service;

import com.zpero.dto.parent.ParentFeedbackDTO;
import com.zpero.vo.parent.ParentLetterVO;

public interface ParentLetterService {

    ParentLetterVO getCurrentParentLetter();

    Long submitFeedback(ParentFeedbackDTO dto);
}
