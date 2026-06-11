package com.zpero.service;

import com.zpero.common.result.PageResult;
import com.zpero.dto.template.TemplateDTO;
import com.zpero.dto.template.TemplateQueryDTO;
import com.zpero.dto.template.TemplateShareDTO;
import com.zpero.vo.template.LetterTemplateVO;

public interface LetterTemplateService {

    PageResult<LetterTemplateVO> listMine(TemplateQueryDTO queryDTO);

    PageResult<LetterTemplateVO> listShared(TemplateQueryDTO queryDTO);

    LetterTemplateVO getById(Long id);

    Long createTemplate(TemplateDTO dto);

    void updateTemplate(Long id, TemplateDTO dto);

    void deleteTemplate(Long id);

    void updateShareStatus(Long id, TemplateShareDTO dto);

    Long copyTemplate(Long id);
}
