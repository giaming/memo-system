package com.gzeic.memosystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gzeic.memosystem.dto.LoginRequest;
import com.gzeic.memosystem.dto.RegisterRequest;
import com.gzeic.memosystem.entity.User;
import com.gzeic.memosystem.exception.BusinessException;
import com.gzeic.memosystem.service.UserService;
import com.gzeic.memosystem.mapper.UserMapper;
import com.gzeic.memosystem.tenant.TenantContext;
import com.gzeic.memosystem.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务实现类
 *
 * 对应需求规格说明书中的 UC-01（用户注册）和 UC-02（用户登录）
 *
 * @author jml
 * @description 针对表【user】的数据库操作Service实现
 * @createDate 2026-02-04 09:40:41
 * @version V1.1 2026-06-07 调整登录返回格式为 {token, userId, username}
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 默认租户ID（用于公开注册和登录场景）
     */
    private static final Long DEFAULT_TENANT_ID = 1L;

    /**
     * Spring中bean注入的方式：
     * 1. 属性注入（字段注入）：@Autowire或@Resource， 在bean的变量上添加，完成依赖注入，缺点是不能够使用final关键字修饰，可能导致NullPointerException
     * 2. setter方法注入：setxx方法()与注解完成依赖注入，缺点是不能保证类的不可变性。
     * 3. 构造器注入：（推荐），优点包括：
     *  - 能够用final关键字修饰变量，保证不可变也不可为空
     *  - 保证类呈现完全初始化状态
     *  - 能够避免循环依赖问题
     */
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    /**
     * 用户注册功能
     *
     * UC-01：用户注册
     *
     * 业务规则：
     * - 用户名不区分大小写，统一转为小写存储
     * - 密码必须使用 BCrypt 加密存储
     * - 用户名在同一租户内唯一
     *
     * @param request 注册请求参数
     * @return 注册成功的用户信息（不含密码）
     */
    @Override
    public User register(RegisterRequest request) {
        Long tenantId = TenantContext.getTenantId();
        // 如果没有租户ID，使用默认租户（适用于公开注册场景）
        if (tenantId == null) {
            tenantId = DEFAULT_TENANT_ID;
            log.warn("未提供租户信息，使用默认租户ID: {}", tenantId);
        }

        // 校验确认密码（业务层校验，前端也需要校验）
        if (request.getPassword() != null && request.getConfirmPassword() != null
                && !request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(400, "两次输入的密码不一致");
        }

        // 检查用户名是否已存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        // 用户名统一转为小写进行存储和查询
        String username = request.getUsername().toLowerCase().trim();
        queryWrapper.eq(User::getUsername, username);
        if (userMapper.exists(queryWrapper)) {
            throw new BusinessException(400, "用户名已被占用");
        }

        // 创建用户
        User user = new User();
        user.setTenantId(tenantId);
        user.setUsername(username);
        // BCrypt 加密密码
        user.setPassword(encoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        userMapper.insert(user);

        log.info("用户注册成功: username={}, tenantId={}", username, tenantId);
        // 返回用户信息时清除密码
        user.setPassword(null);
        return user;
    }

    /**
     * 用户登录功能
     *
     * UC-02：用户登录
     *
     * 返回格式（符合需求文档）：
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIs...",
     *   "userId": 1,
     *   "username": "testuser"
     * }
     *
     * @param request 登录请求参数
     * @return 包含 token、userId、username 的 Map
     */
    @Override
    public Map<String, Object> login(LoginRequest request) {
        // 用户名统一转为小写进行查询
        String username = request.getUsername().toLowerCase().trim();

        // 如果没有租户ID，使用默认租户（与注册逻辑保持一致）
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            tenantId = DEFAULT_TENANT_ID;
            TenantContext.setTenantId(tenantId);
            log.warn("登录时未提供租户信息，使用默认租户ID: {}", tenantId);
        }

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        // 验证用户名
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            log.warn("登录失败：用户不存在，username={}", username);
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 验证密码
        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("登录失败：密码错误，username={}", username);
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 生成 Token（包含 username、userId、tenantId）
        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getTenantId());

        // 构建返回结果（符合需求文档格式）
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());

        log.info("用户登录成功: username={}, userId={}", username, user.getId());
        return result;
    }
}