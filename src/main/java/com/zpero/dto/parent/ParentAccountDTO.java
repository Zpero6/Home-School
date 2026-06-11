package com.zpero.dto.parent;

import lombok.Data;

@Data
public class ParentAccountDTO {
    private Long studentId;
    private String username;
    private String phone;
    private String password;
    private String status;
}
