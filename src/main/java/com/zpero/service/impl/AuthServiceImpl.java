package com.zpero.service.impl;

import com.zpero.common.constant.SecurityConstants;
import com.zpero.common.exception.BusinessException;
import com.zpero.dto.LoginDTO;
import com.zpero.dto.auth.PasswordUpdateDTO;
import com.zpero.entity.SysUser;
import com.zpero.mapper.SysUserMapper;
import com.zpero.security.JwtUtil;
import com.zpero.security.LoginFailureLimiter;
import com.zpero.security.LoginUser;
import com.zpero.security.SecurityUtil;
import com.zpero.service.AuthService;
import com.zpero.service.JwtTokenService;
import com.zpero.vo.LoginVo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String LOGIN_CHANNEL = "pc";

    private final JwtTokenService jwtTokenService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final LoginFailureLimiter loginFailureLimiter;

    @Override
    public LoginVo login(LoginDTO loginDTO) {
        if (loginDTO == null
                || !StringUtils.hasText(loginDTO.getUsername())
                || !StringUtils.hasText(loginDTO.getPassword())) {
            throw new BusinessException(400, "用户名和密码不能为空");
        }

        loginFailureLimiter.assertNotLocked(LOGIN_CHANNEL, loginDTO.getUsername());

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getUsername(),
                            loginDTO.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            if (loginFailureLimiter.recordFailure(LOGIN_CHANNEL, loginDTO.getUsername())) {
                throw new BusinessException(423, "密码错误次数过多，账号已锁定30分钟");
            }
            throw new BusinessException(401, "用户名或密码错误");
        }

        loginFailureLimiter.clear(LOGIN_CHANNEL, loginDTO.getUsername());
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

    @Override
    public void logout(String authorization) {
        if (!StringUtils.hasText(authorization)
                || !authorization.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            throw new BusinessException(401, "用户未登录");
        }
        String token = authorization.substring(SecurityConstants.TOKEN_PREFIX.length());
        Long userId = jwtUtil.getUserId(token);
        String tokenId = jwtUtil.getTokenId(token);
        jwtTokenService.revokeToken(userId, token);
    }

    @Override
    public void updatePassword(PasswordUpdateDTO dto) {
        if (dto == null) {
            throw new BusinessException(400, "密码信息不能为空");
        }
        if (!StringUtils.hasText(dto.getOldPassword())) {
            throw new BusinessException(400, "原密码不能为空");
        }
        if (!StringUtils.hasText(dto.getNewPassword())) {
            throw new BusinessException(400, "新密码不能为空");
        }
        if (dto.getNewPassword().length() < 6) {
            throw new BusinessException(400, "新密码长度不能少于6位");
        }

        SysUser user = sysUserMapper.selectById(SecurityUtil.getCurrentUserId());
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(400, "原密码错误");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        sysUserMapper.updateById(user);
    }
}
