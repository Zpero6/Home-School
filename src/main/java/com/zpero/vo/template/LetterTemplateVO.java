package com.zpero.vo.template;

import com.zpero.entity.LetterTemplate;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LetterTemplateVO {

    private Long id;

    private String name;

    private String content;

    private String backgroundUrl;

    private String logoUrl;

    private Long creatorId;

    private String creatorType;

    private Long collegeId;

    private Integer isShared;

    private Long sourceTemplateId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public LetterTemplateVO(LetterTemplate template) {
        this.id = template.getId();
        this.name = template.getName();
        this.content = template.getContent();
        this.backgroundUrl = template.getBackgroundUrl();
        this.logoUrl = template.getLogoUrl();
        this.creatorId = template.getCreatorId();
        this.creatorType = template.getCreatorType();
        this.collegeId = template.getCollegeId();
        this.isShared = template.getIsShared();
        this.sourceTemplateId = template.getSourceTemplateId();
        this.createTime = template.getCreateTime();
        this.updateTime = template.getUpdateTime();
    }
}
