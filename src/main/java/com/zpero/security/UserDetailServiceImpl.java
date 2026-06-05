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
@RequiredArgsConstructor

/**
*       spring security 的实现逻辑 :
 *        调用 userDetailService 查找用户, 查到的用户放在
 *
* */
public class UserDetailServiceImpl implements UserDetailsService {

    private  final SysUserMapper userMapper;
    private  final SysRoleMapper roleMapper;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                                                    .eq(SysUser::getUsername, username));
        if(sysUser == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        SysRole userRole = roleMapper.selectById(sysUser.getRoleId());

        String roleCode = userRole.getRoleCode();

       List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(roleCode));

       return  new LoginUser(sysUser, roleCode, authorities);

    }
}
