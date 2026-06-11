package com.zpero.dto.college;

import lombok.Data;

@Data
public class CollegeQueryDTO {
    private Long page = 1L;
    private Long size = 10L;
    private String name;
}


