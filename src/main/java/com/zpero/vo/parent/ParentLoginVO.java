package com.zpero.vo.parent;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParentLoginVO {

    private Long parentAccountId;

    private Long studentId;

    private String studentName;

    private String token;
}
