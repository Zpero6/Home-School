package com.zpero.service;


import com.zpero.dto.LoginDTO;
import com.zpero.vo.LoginVo;

public interface AuthService {
    LoginVo login(LoginDTO loginDTO);
}
