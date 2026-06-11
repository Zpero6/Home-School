package com.zpero.vo.Counselor;

import com.zpero.entity.SysUser;
import lombok.Data;

@Data
public class CounselorVo {
    private Long id;
    private String username;
    private String realName;
    private Long collegeId;

    public CounselorVo(SysUser sysUser) {
        this.id = sysUser.getId();
        this.username = sysUser.getUsername();
        this.realName = sysUser.getRealName();
        this.collegeId = sysUser.getCollegeId();
    }
}
