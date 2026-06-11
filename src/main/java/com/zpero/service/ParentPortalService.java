package com.zpero.service;

import com.zpero.dto.parent.ParentPasswordDTO;
import com.zpero.vo.link.ExternalLinkVO;
import com.zpero.vo.score.StudentScoreVO;

import java.util.List;

public interface ParentPortalService {

    List<StudentScoreVO> getCurrentStudentScores();

    void updatePassword(ParentPasswordDTO dto);

    List<ExternalLinkVO> listExternalLinks();
}
