package com.zpero.service;


import com.zpero.entity.ClassInfo;
import com.zpero.entity.College;
import com.zpero.vo.Counselor.CounselorVo;
import com.zpero.vo.student.StatusVo;

import java.util.List;

public interface BasicDataService {
    List<College> listCollege();

    List<ClassInfo> listClassInfo(Long collegeId);

    List<CounselorVo> listCounselors(Long collegeId);

    List<StatusVo> listStudentStatuses();

}
