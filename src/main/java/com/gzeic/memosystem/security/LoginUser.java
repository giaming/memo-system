package com.gzeic.memosystem.security;

import com.gzeic.memosystem.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * 登录用户信息（UserDetails 实现）
 *
 * 设计目的：
 * - Spring Security 需要一个 UserDetails 来表示“当前登录的用户”
 * - 我们把数据库中的 User 实体包装成 LoginUser，交给 SecurityContext 保存
 *
 * 说明（教学版最小实现）：
 * - authorities 目前返回空集合（表示暂不做角色权限）
 * - 后续如果要做 RBAC，可以把 roles/permissions 放到 JWT 或数据库，再组装到 authorities
 */
public class LoginUser implements UserDetails {

    /**
     * 数据库用户实体（包含 tenantId/userId/username 等信息）
     */
    private final User user;

    public LoginUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

