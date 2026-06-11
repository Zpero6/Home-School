package com.zpero.service;


import com.zpero.common.result.PageResult;
import com.zpero.dto.college.CollegeDTO;
import com.zpero.dto.college.CollegeQueryDTO;
import com.zpero.entity.College;

public interface CollegeService {
    PageResult<College> queryPage(CollegeQueryDTO queryDTO);

    College getById(Long id);

    Long createCollege(CollegeDTO collegeDTO);

    void updateCollege(Long id, CollegeDTO collegeDTO);

    void deleteCollege(Long id);

}
