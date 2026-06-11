package com.zpero.service;

import com.zpero.dto.parent.ParentLoginDTO;
import com.zpero.vo.parent.ParentLoginVO;

public interface ParentAuthService {

    ParentLoginVO login(ParentLoginDTO loginDTO);
}
