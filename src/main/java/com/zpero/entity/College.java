package com.zpero.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zpero.entity.baseEntity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("college")
@EqualsAndHashCode(callSuper = true)
public class College extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
}
