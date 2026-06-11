package com.zpero.service;

import com.zpero.dto.award.StudentAwardDTO;
import com.zpero.vo.award.StudentAwardVO;

import java.util.List;

public interface StudentAwardService {

    List<StudentAwardVO> listByStudentId(Long studentId);

    Long createAward(Long studentId, StudentAwardDTO dto);

    void updateAward(Long id, StudentAwardDTO dto);

    void deleteAward(Long id);
}
