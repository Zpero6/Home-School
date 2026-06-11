package com.zpero.service;

import com.zpero.common.result.PageResult;
import com.zpero.dto.letter.LetterQueryDTO;
import com.zpero.dto.letter.LetterResendDTO;
import com.zpero.dto.letter.LetterSendDTO;
import com.zpero.dto.letter.LetterUpdateDTO;
import com.zpero.vo.letter.LetterSendResultVO;
import com.zpero.vo.letter.StudentLetterVO;

public interface StudentLetterService {

    PageResult<StudentLetterVO> queryPage(LetterQueryDTO queryDTO);

    LetterSendResultVO sendLetters(LetterSendDTO dto);

    LetterSendResultVO resendLetters(LetterResendDTO dto);

    void updateLetter(Long id, LetterUpdateDTO dto);
}
