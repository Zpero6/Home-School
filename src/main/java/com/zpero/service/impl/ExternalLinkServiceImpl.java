package com.zpero.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpero.common.exception.BusinessException;
import com.zpero.dto.link.ExternalLinkDTO;
import com.zpero.dto.link.ExternalLinkQueryDTO;
import com.zpero.dto.link.ExternalLinkSortDTO;
import com.zpero.entity.ExternalLink;
import com.zpero.mapper.ExternalLinkMapper;
import com.zpero.security.dataScope.DataScopeProvider;
import com.zpero.service.ExternalLinkService;
import com.zpero.vo.link.ExternalLinkVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExternalLinkServiceImpl implements ExternalLinkService {

    private static final int DEFAULT_SORT = 0;

    private final ExternalLinkMapper externalLinkMapper;
    private final DataScopeProvider dataScopeProvider;

    @Override
    public List<ExternalLinkVO> list(ExternalLinkQueryDTO queryDTO) {
        ExternalLinkQueryDTO query = queryDTO == null ? new ExternalLinkQueryDTO() : queryDTO;

        return externalLinkMapper.selectList(
                        new LambdaQueryWrapper<ExternalLink>()
                                .like(StringUtils.hasText(query.getTitle()),
                                        ExternalLink::getTitle,
                                        query.getTitle())
                                .orderByAsc(ExternalLink::getSort)
                                .orderByDesc(ExternalLink::getId)
                )
                .stream()
                .map(ExternalLinkVO::new)
                .toList();
    }

    @Override
    public Long create(ExternalLinkDTO dto) {
        dataScopeProvider.assertSchool();
        validateDTO(dto);

        ExternalLink link = new ExternalLink();
        link.setTitle(dto.getTitle());
        link.setUrl(dto.getUrl());
        link.setSort(dto.getSort() == null ? DEFAULT_SORT : dto.getSort());
        externalLinkMapper.insert(link);
        return link.getId();
    }

    @Override
    public void update(Long id, ExternalLinkDTO dto) {
        dataScopeProvider.assertSchool();
        validateDTO(dto);

        ExternalLink link = getById(id);
        link.setTitle(dto.getTitle());
        link.setUrl(dto.getUrl());
        link.setSort(dto.getSort() == null ? DEFAULT_SORT : dto.getSort());
        externalLinkMapper.updateById(link);
    }

    @Override
    public void delete(Long id) {
        dataScopeProvider.assertSchool();
        externalLinkMapper.deleteById(getById(id).getId());
    }

    @Override
    public void updateSort(ExternalLinkSortDTO dto) {
        dataScopeProvider.assertSchool();
        if (dto == null || dto.getLinks() == null || dto.getLinks().isEmpty()) {
            throw new BusinessException(400, "链接排序不能为空");
        }

        for (ExternalLinkSortDTO.SortItem item : dto.getLinks()) {
            if (item.getId() == null || item.getSort() == null) {
                throw new BusinessException(400, "链接排序信息不完整");
            }
            ExternalLink link = getById(item.getId());
            link.setSort(item.getSort());
            externalLinkMapper.updateById(link);
        }
    }

    private ExternalLink getById(Long id) {
        if (id == null) {
            throw new BusinessException(400, "链接不能为空");
        }
        ExternalLink link = externalLinkMapper.selectById(id);
        if (link == null) {
            throw new BusinessException(404, "链接不存在");
        }
        return link;
    }

    private void validateDTO(ExternalLinkDTO dto) {
        if (dto == null) {
            throw new BusinessException(400, "链接信息不能为空");
        }
        if (!StringUtils.hasText(dto.getTitle())) {
            throw new BusinessException(400, "链接标题不能为空");
        }
        if (!StringUtils.hasText(dto.getUrl())) {
            throw new BusinessException(400, "链接地址不能为空");
        }
    }
}
