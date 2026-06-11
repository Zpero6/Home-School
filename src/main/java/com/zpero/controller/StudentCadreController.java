package com.zpero.controller;

import com.zpero.common.result.Result;
import com.zpero.dto.cadre.StudentCadreDTO;
import com.zpero.service.StudentCadreService;
import com.zpero.vo.cadre.StudentCadreVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StudentCadreController {

    private final StudentCadreService studentCadreService;

    @GetMapping("/api/v1/students/{studentId}/cadres")
    @PreAuthorize("hasAnyRole('SCHOOL','COLLEGE','COUNSELOR')")
    public Result<List<StudentCadreVO>> listByStudentId(@PathVariable Long studentId) {
        return Result.success(studentCadreService.listByStudentId(studentId));
    }

    @PostMapping("/api/v1/students/{studentId}/cadres")
    @PreAuthorize("hasRole('COUNSELOR')")
    public Result<Long> createCadre(@PathVariable Long studentId,
                                    @RequestBody StudentCadreDTO dto) {
        return Result.success(studentCadreService.createCadre(studentId, dto));
    }

    @PutMapping("/api/v1/cadres/{id}")
    @PreAuthorize("hasRole('COUNSELOR')")
    public Result<Void> updateCadre(@PathVariable Long id,
                                    @RequestBody StudentCadreDTO dto) {
        studentCadreService.updateCadre(id, dto);
        return Result.success();
    }

    @DeleteMapping("/api/v1/cadres/{id}")
    @PreAuthorize("hasRole('COUNSELOR')")
    public Result<Void> deleteCadre(@PathVariable Long id) {
        studentCadreService.deleteCadre(id);
        return Result.success();
    }
}
