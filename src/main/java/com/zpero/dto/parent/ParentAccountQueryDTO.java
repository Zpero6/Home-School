package com.zpero.dto.parent;

import lombok.Data;

@Data
public class ParentAccountQueryDTO {

    private Long page = 1L;

    private Long size = 10L;

    private Long studentId;

    private String username;

    private String phone;

    private Integer status;
}
