package com.zpero.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("external_link")
public class ExternalLink {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String url;

    private Integer sort;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
