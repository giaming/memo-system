package com.gzeic.memosystem.controller;

import com.gzeic.memosystem.dto.LoginRequest;
import com.gzeic.memosystem.dto.RegisterRequest;
import com.gzeic.memosystem.entity.User;
import com.gzeic.memosystem.service.UserService;
import com.gzeic.memosystem.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "用户认证接口", description = "包含注册、登录等认证相关接口")
public class UserController {
    private final UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "传入用户信息，注册新用户")
    public Result<User> register(@Parameter(description = "注册信息", required = true) @Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);
        return Result.success("注册成功", user);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "传入用户名和密码，返回用户信息和token")
    public Result<Map<String, Object>> login(@Parameter(description = "用户登录信息", required = true) @Valid @RequestBody LoginRequest request) {
        Map<String, Object> result = userService.login(request);
        return Result.success("登录成功", result);
    }
}
