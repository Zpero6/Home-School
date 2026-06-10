package com.zpero.service.impl;

import com.zpero.dto.LoginDTO;
import com.zpero.security.JwtUtil;
import com.zpero.security.LoginUser;
import com.zpero.service.AuthService;
import com.zpero.service.JwtTokenService;
import com.zpero.vo.LoginVo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtTokenService jwtTokenService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    public LoginVo login(LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getUsername(),
                        loginDTO.getPassword()
                )
        );
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();

        String tokenId = UUID.randomUUID().toString();

        String token = jwtUtil.generateToken(
                loginUser.getUser().getId(),
                loginUser.getUsername(),
                loginUser.getRoleCode(),
                tokenId);

        jwtTokenService.storeToken(loginUser.getUser().getId(), tokenId, loginUser);
        return LoginVo.builder()
                .id(loginUser.getUser().getId())
                .username(loginUser.getUsername())
                .roleCode(loginUser.getRoleCode())
                .token(token)
                .build();
    }



}
