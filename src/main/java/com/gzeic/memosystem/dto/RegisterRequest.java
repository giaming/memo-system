package com.gzeic.memosystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求DTO
 */
@Data
public class RegisterRequest {
    @NotBlank(message = "用户名不为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50之间")
    private String username;
    @NotBlank(message = "密码不为空")
    @Size(min = 5, max = 50, message = "密码长度必须在6-50之间")
    private String password;
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    private String email;

    private String phone;
}
