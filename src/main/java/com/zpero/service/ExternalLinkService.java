package com.zpero.service;

import com.zpero.dto.link.ExternalLinkDTO;
import com.zpero.dto.link.ExternalLinkQueryDTO;
import com.zpero.dto.link.ExternalLinkSortDTO;
import com.zpero.vo.link.ExternalLinkVO;

import java.util.List;

public interface ExternalLinkService {

    List<ExternalLinkVO> list(ExternalLinkQueryDTO queryDTO);

    Long create(ExternalLinkDTO dto);

    void update(Long id, ExternalLinkDTO dto);

    void delete(Long id);

    void updateSort(ExternalLinkSortDTO dto);
}
