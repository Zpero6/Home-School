package com.zpero.service;


import com.zpero.common.result.PageResult;
import com.zpero.dto.parent.ParentAccountDTO;
import com.zpero.dto.parent.ParentAccountQueryDTO;
import com.zpero.vo.parent.ParentAccountVO;

public interface ParentAccountService {
    PageResult<ParentAccountVO> queryPage(ParentAccountQueryDTO queryDTO);

    ParentAccountVO getById(Long id);

    Long createParentAccount(ParentAccountDTO parentAccountDTO);

    void updateParentAccount(Long id,ParentAccountDTO parentAccountDTO);

    void deleteParentAccount(Long id);
}
