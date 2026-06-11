package com.zpero.vo.link;

import com.zpero.entity.ExternalLink;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExternalLinkVO {

    private Long id;

    private String title;

    private String url;

    private Integer sort;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public ExternalLinkVO(ExternalLink link) {
        this.id = link.getId();
        this.title = link.getTitle();
        this.url = link.getUrl();
        this.sort = link.getSort();
        this.createTime = link.getCreateTime();
        this.updateTime = link.getUpdateTime();
    }
}
