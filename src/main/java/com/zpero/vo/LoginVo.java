package com.zpero.vo;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
public class LoginVo {

    private Long id;
    private String username;

    private String roleCode;
    private String token;

}
