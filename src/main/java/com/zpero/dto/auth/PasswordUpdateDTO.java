package com.zpero.dto.auth;

import lombok.Data;

@Data
public class PasswordUpdateDTO {

    private String oldPassword;

    private String newPassword;
}
