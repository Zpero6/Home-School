package com.zpero.service;


import com.zpero.entity.ClassInfo;
import com.zpero.entity.College;

import java.util.List;

public interface BasicDataService {
    List<College> listCollege();

    List<ClassInfo> listClassInfo(Long collegeId);

}
