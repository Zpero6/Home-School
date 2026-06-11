package com.zpero.service;

import com.zpero.dto.score.StudentScoreDTO;
import com.zpero.vo.score.ScoreImportResultVO;
import com.zpero.vo.score.StudentScoreVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StudentScoreService {

    List<StudentScoreVO> listByStudentId(Long studentId);

    Long createScore(Long studentId, StudentScoreDTO dto);

    void updateScore(Long id, StudentScoreDTO dto);

    void deleteScore(Long id);

    ScoreImportResultVO importScores(MultipartFile file);
}
