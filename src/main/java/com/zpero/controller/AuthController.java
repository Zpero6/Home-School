package com.zpero.controller;

import com.zpero.common.constant.SecurityConstants;
import com.zpero.common.result.Result;
import com.zpero.dto.LoginDTO;
import com.zpero.service.AuthService;
import com.zpero.vo.LoginVo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("auth/login")
    public Result<LoginVo> login(@RequestBody LoginDTO loginDTO) {
        LoginVo loginVo = authService.login(loginDTO);
       return Result.success(loginVo);

    }

    @PostMapping("auth/logout")
    public Result<Void> logout(HttpServletRequest request) {
        authService.logout(request.getHeader(SecurityConstants.HEADER));
        return Result.success();

    }
}
