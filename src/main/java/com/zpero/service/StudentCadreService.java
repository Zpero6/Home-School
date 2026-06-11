package com.zpero.service;

import com.zpero.dto.cadre.StudentCadreDTO;
import com.zpero.vo.cadre.StudentCadreVO;

import java.util.List;

public interface StudentCadreService {

    List<StudentCadreVO> listByStudentId(Long studentId);

    Long createCadre(Long studentId, StudentCadreDTO dto);

    void updateCadre(Long id, StudentCadreDTO dto);

    void deleteCadre(Long id);
}
