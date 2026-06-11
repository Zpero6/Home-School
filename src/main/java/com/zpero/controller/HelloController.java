package com.zpero.controller;

import com.zpero.common.result.Result;
import com.zpero.dto.CurrentLoginUser;
import com.zpero.security.SecurityUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello, Spring Boot 3.5.14 with Java 21!";
    }

    @GetMapping("/me")
    public Result<CurrentLoginUser> me() {
        return Result.success(SecurityUtil.getCurrentUser());
    }

    @GetMapping("/school")
    @PreAuthorize("hasRole('SCHOOL')")
    public Result<String> schoolOnly() {
        return Result.success("school ok");
    }

    @GetMapping("/college")
    @PreAuthorize("hasAnyRole('SCHOOL', 'COLLEGE')")
    public Result<String> collegeOrSchool() {
        return Result.success("college ok");
    }
}
