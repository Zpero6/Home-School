package com.zpero.service;

import com.zpero.dto.CurrentLoginUser;
import com.zpero.security.LoginUser;
import org.springframework.stereotype.Service;

public interface JwtTokenService {

    void storeToken(Long userId, String tokenId, LoginUser loginUser) ;

    CurrentLoginUser getLoginUser(Long userId, String tokenId) ;

    void revokeToken(Long userId, String tokenId) ;

    void revokeAllTokens(Long userId) ;



}
