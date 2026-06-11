package com.zpero.service;


import com.zpero.dto.LoginDTO;
import com.zpero.dto.auth.PasswordUpdateDTO;
import com.zpero.vo.LoginVo;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    LoginVo login(LoginDTO loginDTO);

    void logout(String authorization);

    void updatePassword(PasswordUpdateDTO dto);
}
