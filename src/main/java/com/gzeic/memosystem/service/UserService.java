package com.gzeic.memosystem.service;

import com.gzeic.memosystem.dto.LoginRequest;
import com.gzeic.memosystem.dto.RegisterRequest;
import com.gzeic.memosystem.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
* @author jml
* @description 针对表【user】的数据库操作Service
* @createDate 2026-02-04 09:40:41
*/
public interface UserService extends IService<User> {
    User register(RegisterRequest request);

    Map<String, Object> login(LoginRequest request);
}
