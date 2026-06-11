package com.zpero.controller;

import com.zpero.common.result.Result;
import com.zpero.dto.parent.ParentLoginDTO;
import com.zpero.service.ParentAuthService;
import com.zpero.vo.parent.ParentLoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/parent")
@RequiredArgsConstructor
public class ParentAuthController {

    private final ParentAuthService parentAuthService;

    @PostMapping("/login")
    public Result<ParentLoginVO> login(@RequestBody ParentLoginDTO loginDTO) {
        return Result.success(parentAuthService.login(loginDTO));
    }
}
