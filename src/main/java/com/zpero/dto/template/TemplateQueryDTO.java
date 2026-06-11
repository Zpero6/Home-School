package com.zpero.dto.template;

import lombok.Data;

@Data
public class TemplateQueryDTO {

    private Long page = 1L;

    private Long size = 10L;

    private String name;
}
