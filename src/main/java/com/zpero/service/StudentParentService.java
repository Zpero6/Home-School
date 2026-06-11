package com.zpero.service;

import com.zpero.dto.parent.StudentParentDTO;
import com.zpero.entity.StudentParent;

import java.util.List;

public interface StudentParentService {

    List<StudentParent> listByStudentId(Long studentId);

    Long createStudentParent(Long studentId, StudentParentDTO dto);

    void updateStudentParent(Long id, StudentParentDTO dto);

    void deleteStudentParent(Long id);

    void setDefaultParent(Long id);
}
