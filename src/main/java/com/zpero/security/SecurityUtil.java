package com.zpero.security;

import com.zpero.dto.CurrentLoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {
    /*
    *      SecurityContextHandler.getContext() : Security 默认使用ThreadLocal 存储用户安全信息,
    *           当前处理的线程中一直存储这份上下文
    *      .getAuthentication() : 从上下文中获取认证对象
    *                       认证对象代表了当前的认证状态,如果已经登录完成,这个对象回存储它的权限列表,凭证和身份信息
    *      .getPrincipal() : 获取身份的主体(principal)并转换成 CurrentLoginUser 对象
    *                       默认返回一个object对象, 本项目在 JWT 过滤器中放入的是当前登录用户快照
    *
    * */

    /**
     *      获取当前登录用户
     *      从 SecurityContext 里面取出登录用户的Authentication对象
     *      getPrincipal() 方法返回的是的登录信息
     * @return
     */
    public static CurrentLoginUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder
                                             .getContext()
                                             .getAuthentication();
        return (CurrentLoginUser) authentication.getPrincipal();
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().getUserId();
    }
    public static String getCurrentUserName() {
        return getCurrentUser().getUserName();
    }

    public static String getCurrentUserRoleCode() {
        return getCurrentUser().getRoleCode();
    }

    public static Long getCurrentUserCollegeId() {
        return getCurrentUser().getCollegeId();
    }

}
