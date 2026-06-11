package com.zpero.service;

import com.zpero.dto.score.StudentScoreDTO;
import com.zpero.vo.score.StudentScoreVO;

import java.util.List;

public interface StudentScoreService {

    List<StudentScoreVO> listByStudentId(Long studentId);

    Long createScore(Long studentId, StudentScoreDTO dto);

    void updateScore(Long id, StudentScoreDTO dto);

    void deleteScore(Long id);
}
