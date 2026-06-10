package com.zpero.config;


import com.zpero.dto.CurrentLoginUser;
import com.zpero.security.JwtUtil;
import com.zpero.service.JwtTokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import com.zpero.common.constant.SecurityConstant;
import java.io.IOException;
import java.util.List;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final JwtTokenService jwtTokenService;

    // 拦截请求, 要求检查 是否携带token, 放行
    // token 包含 userId, userName , roleCode , tokenId
    @Override
    public void doFilterInternal(HttpServletRequest request,
                                 HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {


        String authorization = request.getHeader(SecurityConstant.HEADER);

        if(!StringUtils.hasText(authorization)
                || !authorization.startsWith(SecurityConstant.TOKEN_PREFIX)){

            chain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(SecurityConstant.TOKEN_PREFIX.length());

        try{

            Claims claims = jwtUtil.parseToken(token);
            Long userId  = ((Number)  claims.get("id")).longValue();
            String tokenId = claims.get("tokenId", String.class);

            CurrentLoginUser loginUser = jwtTokenService.getLoginUser(userId,tokenId);

            if(loginUser == null){
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    loginUser,
                    null,
                    List.of(new SimpleGrantedAuthority(loginUser.getRoleCode())));

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        }catch (Exception e){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return ;

        }

        chain.doFilter(request, response);
    }

}
