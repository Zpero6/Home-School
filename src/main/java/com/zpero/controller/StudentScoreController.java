package com.zpero.controller;

import com.zpero.common.result.Result;
import com.zpero.dto.score.StudentScoreDTO;
import com.zpero.service.StudentScoreService;
import com.zpero.vo.score.ScoreImportResultVO;
import com.zpero.vo.score.StudentScoreVO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StudentScoreController {

    private final StudentScoreService studentScoreService;

    @GetMapping("/api/v1/students/{studentId}/scores")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE','COUNSELOR')")
    public Result<List<StudentScoreVO>> listByStudentId(@PathVariable Long studentId) {
        return Result.success(studentScoreService.listByStudentId(studentId));
    }

    @PostMapping("/api/v1/students/{studentId}/scores")
    @PreAuthorize("hasRole('COUNSELOR')")
    public Result<Long> createScore(@PathVariable Long studentId,
                                    @RequestBody StudentScoreDTO dto) {
        return Result.success(studentScoreService.createScore(studentId, dto));
    }

    @PostMapping(value = "/api/v1/scores/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('COUNSELOR')")
    public Result<ScoreImportResultVO> importScores(@RequestParam("file") MultipartFile file) {
        return Result.success(studentScoreService.importScores(file));
    }

    @PutMapping("/api/v1/scores/{id}")
    @PreAuthorize("hasRole('COUNSELOR')")
    public Result<Void> updateScore(@PathVariable Long id,
                                    @RequestBody StudentScoreDTO dto) {
        studentScoreService.updateScore(id, dto);
        return Result.success();
    }

    @DeleteMapping("/api/v1/scores/{id}")
    @PreAuthorize("hasRole('COUNSELOR')")
    public Result<Void> deleteScore(@PathVariable Long id) {
        studentScoreService.deleteScore(id);
        return Result.success();
    }
}
