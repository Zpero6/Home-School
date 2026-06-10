package com.zpero.security;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zpero.entity.SysRole;
import com.zpero.entity.SysUser;
import com.zpero.mapper.SysRoleMapper;
import com.zpero.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
// 使用构造器注入 , 定义几个 final 的注入对象即可
@RequiredArgsConstructor

/**
 *       spring security 的实现逻辑 :
 *        调用 userDetailService 查找用户, 查到的用户放在UserDetails 对象中
 *          找到的用户信息放在UserDetails中, 用户权限放在 里面的权限列表里, userDetailService 就是数据库和Security 之间的桥梁
 *              UserDetails 包装成 Authentication 认证对象 , 会放在Security Context 里, 用于后面的业务
 *          本项目使用了 LoginUser 实现了UserDetails 接口, 用LoginUser 指代 UserDetails
 *
 * */
public class UserDetailServiceImpl implements UserDetailsService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, username));
        if (sysUser == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        SysRole userRole = roleMapper.selectById(sysUser.getRoleId());

        String roleCode = userRole.getRoleCode();

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(roleCode));

        return new LoginUser(sysUser, roleCode, authorities);

    }
}
