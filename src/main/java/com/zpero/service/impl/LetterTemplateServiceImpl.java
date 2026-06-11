package com.zpero.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zpero.common.exception.BusinessException;
import com.zpero.common.result.PageResult;
import com.zpero.dto.template.TemplateDTO;
import com.zpero.dto.template.TemplateQueryDTO;
import com.zpero.dto.template.TemplateShareDTO;
import com.zpero.entity.LetterTemplate;
import com.zpero.mapper.LetterTemplateMapper;
import com.zpero.security.SecurityUtil;
import com.zpero.service.LetterTemplateService;
import com.zpero.vo.template.LetterTemplateVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class LetterTemplateServiceImpl implements LetterTemplateService {

    private static final int SHARED = 1;
    private static final int NOT_SHARED = 0;
    private static final String CREATOR_SCHOOL = "SCHOOL";
    private static final String CREATOR_COLLEGE = "COLLEGE";
    private static final String CREATOR_COUNSELOR = "COUNSELOR";

    private final LetterTemplateMapper letterTemplateMapper;

    @Override
    public PageResult<LetterTemplateVO> listMine(TemplateQueryDTO queryDTO) {
        TemplateQueryDTO query = defaultQuery(queryDTO);
        LambdaQueryWrapper<LetterTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LetterTemplate::getCreatorId, SecurityUtil.getCurrentUserId())
                .eq(LetterTemplate::getCreatorType, currentCreatorType())
                .like(StringUtils.hasText(query.getName()), LetterTemplate::getName, query.getName())
                .orderByDesc(LetterTemplate::getUpdateTime)
                .orderByDesc(LetterTemplate::getId);

        Page<LetterTemplate> page = letterTemplateMapper.selectPage(
                new Page<>(query.getPage(), query.getSize()),
                wrapper
        );
        return toPageResult(page);
    }

    @Override
    public PageResult<LetterTemplateVO> listShared(TemplateQueryDTO queryDTO) {
        TemplateQueryDTO query = defaultQuery(queryDTO);
        LambdaQueryWrapper<LetterTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LetterTemplate::getIsShared, SHARED)
                .ne(LetterTemplate::getCreatorId, SecurityUtil.getCurrentUserId())
                .like(StringUtils.hasText(query.getName()), LetterTemplate::getName, query.getName());

        applySharedVisibleScope(wrapper);

        wrapper.orderByDesc(LetterTemplate::getUpdateTime)
                .orderByDesc(LetterTemplate::getId);

        Page<LetterTemplate> page = letterTemplateMapper.selectPage(
                new Page<>(query.getPage(), query.getSize()),
                wrapper
        );
        return toPageResult(page);
    }

    @Override
    public LetterTemplateVO getById(Long id) {
        LetterTemplate template = getAccessibleTemplate(id);
        return new LetterTemplateVO(template);
    }

    @Override
    public Long createTemplate(TemplateDTO dto) {
        validateTemplateDTO(dto);

        LetterTemplate template = new LetterTemplate();
        fillEditableFields(template, dto);
        template.setCreatorId(SecurityUtil.getCurrentUserId());
        template.setCreatorType(currentCreatorType());
        template.setCollegeId(currentTemplateCollegeId());
        template.setIsShared(NOT_SHARED);

        letterTemplateMapper.insert(template);
        return template.getId();
    }

    @Override
    public void updateTemplate(Long id, TemplateDTO dto) {
        validateTemplateDTO(dto);

        LetterTemplate template = getOwnedTemplate(id);
        fillEditableFields(template, dto);
        letterTemplateMapper.updateById(template);
    }

    @Override
    public void deleteTemplate(Long id) {
        LetterTemplate template = getOwnedTemplate(id);
        letterTemplateMapper.deleteById(template.getId());
    }

    @Override
    public void updateShareStatus(Long id, TemplateShareDTO dto) {
        if (dto == null || dto.getIsShared() == null) {
            throw new BusinessException(400, "共享状态不能为空");
        }
        if (dto.getIsShared() != SHARED && dto.getIsShared() != NOT_SHARED) {
            throw new BusinessException(400, "共享状态只能是0或1");
        }

        LetterTemplate template = getOwnedTemplate(id);
        template.setIsShared(dto.getIsShared());
        letterTemplateMapper.updateById(template);
    }

    @Override
    public Long copyTemplate(Long id) {
        LetterTemplate source = getAccessibleTemplate(id);

        LetterTemplate copied = new LetterTemplate();
        copied.setName(source.getName() + "副本");
        copied.setContent(source.getContent());
        copied.setBackgroundUrl(source.getBackgroundUrl());
        copied.setLogoUrl(source.getLogoUrl());
        copied.setCreatorId(SecurityUtil.getCurrentUserId());
        copied.setCreatorType(currentCreatorType());
        copied.setCollegeId(currentTemplateCollegeId());
        copied.setIsShared(NOT_SHARED);
        copied.setSourceTemplateId(source.getId());

        letterTemplateMapper.insert(copied);
        return copied.getId();
    }

    private LetterTemplate getAccessibleTemplate(Long id) {
        LetterTemplate template = getTemplateById(id);
        if (isOwnedByCurrentUser(template) || canViewSharedTemplate(template)) {
            return template;
        }
        throw new BusinessException(403, "无权访问模板");
    }

    private LetterTemplate getOwnedTemplate(Long id) {
        LetterTemplate template = getTemplateById(id);
        if (!isOwnedByCurrentUser(template)) {
            throw new BusinessException(403, "只能操作自己创建的模板");
        }
        return template;
    }

    private LetterTemplate getTemplateById(Long id) {
        if (id == null) {
            throw new BusinessException(400, "模板不能为空");
        }

        LetterTemplate template = letterTemplateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException(404, "模板不存在");
        }
        return template;
    }

    private void fillEditableFields(LetterTemplate template, TemplateDTO dto) {
        template.setName(dto.getName());
        template.setContent(dto.getContent());
        template.setBackgroundUrl(dto.getBackgroundUrl());
        template.setLogoUrl(dto.getLogoUrl());
    }

    private void validateTemplateDTO(TemplateDTO dto) {
        if (dto == null) {
            throw new BusinessException(400, "模板信息不能为空");
        }
        if (!StringUtils.hasText(dto.getName())) {
            throw new BusinessException(400, "模板名称不能为空");
        }
        if (!StringUtils.hasText(dto.getContent())) {
            throw new BusinessException(400, "模板内容不能为空");
        }
    }

    private boolean isOwnedByCurrentUser(LetterTemplate template) {
        return SecurityUtil.getCurrentUserId().equals(template.getCreatorId())
                && currentCreatorType().equals(template.getCreatorType());
    }

    private boolean canViewSharedTemplate(LetterTemplate template) {
        if (template.getIsShared() == null || template.getIsShared() != SHARED) {
            return false;
        }

        String creatorType = currentCreatorType();
        if (CREATOR_SCHOOL.equals(creatorType)) {
            return true;
        }
        if (CREATOR_SCHOOL.equals(template.getCreatorType())) {
            return true;
        }
        return SecurityUtil.getCurrentUserCollegeId() != null
                && SecurityUtil.getCurrentUserCollegeId().equals(template.getCollegeId());
    }

    private void applySharedVisibleScope(LambdaQueryWrapper<LetterTemplate> wrapper) {
        String creatorType = currentCreatorType();

        if (CREATOR_SCHOOL.equals(creatorType)) {
            return;
        }

        Long collegeId = SecurityUtil.getCurrentUserCollegeId();
        wrapper.and(scope -> scope
                .eq(LetterTemplate::getCreatorType, CREATOR_SCHOOL)
                .or()
                .eq(LetterTemplate::getCollegeId, collegeId)
        );
    }

    private String currentCreatorType() {
        String roleCode = SecurityUtil.getCurrentUserRoleCode();
        if ("ROLE_SCHOOL".equals(roleCode)) {
            return CREATOR_SCHOOL;
        }
        if ("ROLE_COLLEGE".equals(roleCode)) {
            return CREATOR_COLLEGE;
        }
        if ("ROLE_COUNSELOR".equals(roleCode)) {
            return CREATOR_COUNSELOR;
        }
        throw new BusinessException(403, "当前角色不能管理模板");
    }

    private Long currentTemplateCollegeId() {
        if (CREATOR_SCHOOL.equals(currentCreatorType())) {
            return null;
        }
        return SecurityUtil.getCurrentUserCollegeId();
    }

    private TemplateQueryDTO defaultQuery(TemplateQueryDTO queryDTO) {
        return queryDTO == null ? new TemplateQueryDTO() : queryDTO;
    }

    private PageResult<LetterTemplateVO> toPageResult(Page<LetterTemplate> page) {
        PageResult<LetterTemplateVO> result = new PageResult<>();
        result.setPage(page.getCurrent());
        result.setSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setRecords(page.getRecords()
                .stream()
                .map(LetterTemplateVO::new)
                .toList());
        return result;
    }
}
