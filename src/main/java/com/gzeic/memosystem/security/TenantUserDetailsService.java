package com.gzeic.memosystem.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gzeic.memosystem.entity.User;
import com.gzeic.memosystem.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 多租户版 UserDetailsService
 *
 * 关键点：
 * - Spring Security 在需要加载用户信息时，会调用 loadUserByUsername
 * - 我们这里用 MyBatis-Plus 查询用户
 * - 多租户隔离由 MyBatis-Plus TenantLineInnerInterceptor 自动保证（SQL 自动带 tenant_id 条件）
 *
 * 教学提示：
 * - 你会发现这里没有手写 tenant_id 条件，这是因为我们已经把“多租户隔离”下沉到 MP 插件层
 */
@Service
@RequiredArgsConstructor
public class TenantUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);

        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        return new LoginUser(user);
    }
}

