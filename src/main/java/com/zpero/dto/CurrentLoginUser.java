package com.zpero.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CurrentLoginUser {

    private Long userId;
    private String userName;
    private String realName;
    private String roleCode;
    private Long collegeId;
}
