package com.zpero.controller;

import com.zpero.common.result.PageResult;
import com.zpero.common.result.Result;
import com.zpero.dto.letter.LetterQueryDTO;
import com.zpero.dto.letter.LetterResendDTO;
import com.zpero.dto.letter.LetterSendDTO;
import com.zpero.dto.letter.LetterUpdateDTO;
import com.zpero.service.StudentLetterService;
import com.zpero.vo.letter.LetterSendResultVO;
import com.zpero.vo.letter.StudentLetterVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/letters")
@RequiredArgsConstructor
public class StudentLetterController {

    private final StudentLetterService studentLetterService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE','COUNSELOR')")
    public Result<PageResult<StudentLetterVO>> queryPage(LetterQueryDTO queryDTO) {
        return Result.success(studentLetterService.queryPage(queryDTO));
    }

    @PostMapping("/send")
    @PreAuthorize("hasRole('COUNSELOR')")
    public Result<LetterSendResultVO> sendLetters(@RequestBody LetterSendDTO dto) {
        return Result.success(studentLetterService.sendLetters(dto));
    }

    @PostMapping("/resend")
    @PreAuthorize("hasRole('COUNSELOR')")
    public Result<LetterSendResultVO> resendLetters(@RequestBody LetterResendDTO dto) {
        return Result.success(studentLetterService.resendLetters(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('COUNSELOR')")
    public Result<Void> updateLetter(@PathVariable Long id,
                                     @RequestBody LetterUpdateDTO dto) {
        studentLetterService.updateLetter(id, dto);
        return Result.success();
    }
}
