package com.zpero.vo.parent;

import com.zpero.entity.ParentAccount;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ParentAccountVO {

    private Long id;

    private Long studentId;

    private String username;

    private LocalDateTime lastLoginTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public ParentAccountVO(ParentAccount account) {
        this.id = account.getId();
        this.studentId = account.getStudentId();
        this.username = account.getUsername();
        this.lastLoginTime = account.getLastLoginTime();
        this.createTime = account.getCreateTime();
        this.updateTime = account.getUpdateTime();
    }
}
