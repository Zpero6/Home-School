package com.zpero.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zpero.common.exception.BusinessException;
import com.zpero.dto.CurrentLoginUser;
import com.zpero.security.LoginUser;
import com.zpero.service.JwtTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class JwtTokenServiceImpl implements JwtTokenService {

    private static final String TOKEN_PREFIX = "jwt:token:";
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    @Value("${jwt.expiration-time}")
    private Long expireTime;

    @Override
    public void storeToken(Long userId, String tokenId, LoginUser loginUser) {
        CurrentLoginUser cache = CurrentLoginUser.builder()
                .userId(userId)
                .userName(loginUser.getUsername())
                .realName(loginUser.getUser().getRealName())
                .roleCode(loginUser.getRoleCode())
                .collegeId(loginUser.getUser().getCollegeId())
                .build();

        storeCurrentUser(userId, tokenId, cache);
    }

    @Override
    public void storeCurrentUser(Long userId, String tokenId, CurrentLoginUser currentLoginUser) {
        try {
            String key = TOKEN_PREFIX + userId + ":" + tokenId;
            String value = objectMapper.writeValueAsString(currentLoginUser);
            redisTemplate.opsForValue().set(
                    key, value, Duration.ofMillis(expireTime));


        } catch (JsonProcessingException jsonProcessingException) {
            log.error("登录用户信息缓存失败", jsonProcessingException);
            throw new BusinessException(500, "登录用户信息缓存失败");

        }
    }

    @Override
    public CurrentLoginUser getLoginUser(Long userId, String tokenId) {
        String key = TOKEN_PREFIX + userId + ":" + tokenId;
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        try{
            return objectMapper.readValue(value,CurrentLoginUser.class);
        }catch (JsonProcessingException e){
            log.error("登录用户信息解析失败", e);
            throw  new BusinessException(500,"登录信息解析失败");
        }
    }

    @Override
    public void revokeToken(Long userId, String tokenId) {
        redisTemplate.delete(TOKEN_PREFIX + userId + ":" + tokenId);

    }

    @Override
    public void revokeAllTokens(Long userId) {
        Set<String> keys = redisTemplate.keys(TOKEN_PREFIX + userId);
        if(!keys.isEmpty() && keys != null){
            redisTemplate.delete(keys);
        }

    }

}
